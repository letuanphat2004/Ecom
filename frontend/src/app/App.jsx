import { useEffect, useState } from 'react';
import { Link, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { LogOut, ShoppingBag } from 'lucide-react';
import AdminInventoryPage from '../pages/AdminInventoryPage.jsx';
import CartPage from '../pages/CartPage.jsx';
import LoginPage from '../pages/LoginPage.jsx';
import OrdersPage from '../pages/OrdersPage.jsx';
import RegisterPage from '../pages/RegisterPage.jsx';
import ShopPage from '../pages/ShopPage.jsx';
import { apiRequest } from '../services/api.js';

const AUTH_STORAGE_KEY = 'ecom_auth';

export default function App() {
  const [auth, setAuth] = useState(() => {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  });
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [orders, setOrders] = useState([]);
  const [message, setMessage] = useState('');

  const token = auth?.token;
  const defaultPath = getDefaultPath(auth);

  useEffect(() => {
    loadProducts();
  }, []);

  useEffect(() => {
    if (auth) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
      if (auth.user.roles.includes('ROLE_CUSTOMER')) {
        loadOrders(auth.token);
      }
    } else {
      localStorage.removeItem(AUTH_STORAGE_KEY);
      setOrders([]);
    }
  }, [auth]);

  async function loadProducts() {
    try {
      setProducts(await apiRequest('/products'));
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function loadOrders(currentToken = token) {
    if (!currentToken) return;

    try {
      setOrders(await apiRequest('/orders/me', {}, currentToken));
    } catch (error) {
      setMessage(error.message);
    }
  }

  function handleAuth(nextAuth) {
    setAuth(nextAuth);
    setMessage('Dang nhap thanh cong.');
  }

  function addToCart(product) {
    setCart((current) => {
      const existing = current.find((item) => item.id === product.id);
      if (existing) {
        return current.map((item) =>
          item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item,
        );
      }
      return [...current, { ...product, quantity: 1 }];
    });
    setMessage('Da them san pham vao gio hang.');
  }

  function removeFromCart(productId) {
    setCart((current) => current.filter((item) => item.id !== productId));
  }

  async function submitOrder() {
    if (!token) {
      setMessage('Vui long dang nhap truoc khi dat hang.');
      return false;
    }
    if (cart.length === 0) {
      setMessage('Gio hang dang trong.');
      return false;
    }

    try {
      await apiRequest(
        '/orders',
        {
          method: 'POST',
          body: JSON.stringify({
            items: cart.map((item) => ({ productId: item.id, quantity: item.quantity })),
          }),
        },
        token,
      );
      setCart([]);
      setMessage('Dat hang thanh cong.');
      await loadProducts();
      await loadOrders();
      return true;
    } catch (error) {
      setMessage(error.message);
      return false;
    }
  }

  function logout() {
    setAuth(null);
    setCart([]);
    setMessage('Da dang xuat.');
  }

  return (
    <main className="app-shell">
      <AppHeader auth={auth} cartCount={cart.length} onLogout={logout} />

      {message && (
        <button className="notice" onClick={() => setMessage('')}>
          {message}
        </button>
      )}

      <Routes>
        <Route path="/" element={<Navigate to={auth ? defaultPath : '/login'} replace />} />
        <Route
          path="/login"
          element={
            auth ? <Navigate to={defaultPath} replace /> : <LoginPage onAuth={handleAuth} onMessage={setMessage} />
          }
        />
        <Route
          path="/register"
          element={
            auth ? <Navigate to={defaultPath} replace /> : <RegisterPage onAuth={handleAuth} onMessage={setMessage} />
          }
        />
        <Route
          path="/shop"
          element={
            <ProtectedRoute auth={auth} role="ROLE_CUSTOMER" fallback="/admin/inventory">
              <ShopPage products={products} onAddToCart={addToCart} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/cart"
          element={
            <ProtectedRoute auth={auth} role="ROLE_CUSTOMER" fallback="/admin/inventory">
              <CartPage cart={cart} onRemove={removeFromCart} onSubmit={submitOrder} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/orders"
          element={
            <ProtectedRoute auth={auth} role="ROLE_CUSTOMER" fallback="/admin/inventory">
              <OrdersPage orders={orders} onRefresh={() => loadOrders()} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/inventory"
          element={
            <ProtectedRoute auth={auth} role="ROLE_ADMIN" fallback="/shop">
              <AdminInventoryPage token={token} onMessage={setMessage} />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </main>
  );
}

function ProtectedRoute({ auth, role, fallback, children }) {
  const location = useLocation();

  if (!auth) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (role && !auth.user.roles.includes(role)) {
    return <Navigate to={fallback || getDefaultPath(auth)} replace />;
  }

  return children;
}

function getDefaultPath(auth) {
  if (!auth) return '/login';
  if (auth.user.roles.includes('ROLE_ADMIN')) return '/admin/inventory';
  return '/shop';
}

function AppHeader({ auth, cartCount, onLogout }) {
  const navigate = useNavigate();
  const isAdmin = auth?.user?.roles?.includes('ROLE_ADMIN');
  const isCustomer = auth?.user?.roles?.includes('ROLE_CUSTOMER');

  function logout() {
    onLogout();
    navigate('/login');
  }

  return (
    <header className="topbar">
      <Link className="brand-link" to={auth ? getDefaultPath(auth) : '/login'}>
        <p className="eyebrow">Monolith commerce</p>
        <h1>Ecom Store</h1>
      </Link>

      <nav className="nav-links">
        {auth ? (
          <>
            {isCustomer && (
              <>
                <Link to="/shop">Mua hang</Link>
                <Link to="/cart">
                  <ShoppingBag size={17} />
                  Gio hang ({cartCount})
                </Link>
                <Link to="/orders">Don hang</Link>
              </>
            )}
            {isAdmin && <Link to="/admin/inventory">Kho hang</Link>}
            <div className="user-box">
              <span>{auth.user.fullName}</span>
              <button className="icon-button" title="Dang xuat" onClick={logout}>
                <LogOut size={18} />
              </button>
            </div>
          </>
        ) : (
          <>
            <Link to="/login">Dang nhap</Link>
            <Link to="/register">Dang ky</Link>
          </>
        )}
      </nav>
    </header>
  );
}
