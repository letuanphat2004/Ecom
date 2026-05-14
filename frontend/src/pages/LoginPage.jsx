import { Link, useLocation, useNavigate } from 'react-router-dom';
import AuthForm from '../components/AuthForm.jsx';
import { apiRequest } from '../services/api.js';

export default function LoginPage({ onAuth, onMessage }) {
  const navigate = useNavigate();
  const location = useLocation();
  const redirectTo = location.state?.from?.pathname || '/shop';

  async function login(form) {
    try {
      const response = await apiRequest('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email: form.email, password: form.password }),
      });
      onAuth(response);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      onMessage(error.message);
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">Tai khoan</p>
        <h2>Dang nhap</h2>
        <AuthForm mode="login" onSubmit={login} submitLabel="Dang nhap" />
        <p className="auth-switch">
          Chua co tai khoan? <Link to="/register">Dang ky</Link>
        </p>
      </div>
    </section>
  );
}
