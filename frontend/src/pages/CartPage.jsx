import CartPanel from '../components/CartPanel.jsx';
import { formatCurrency } from '../utils/formatCurrency.js';

export default function CartPage({ cart, onRemove, onSubmit }) {
  const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

  return (
    <section className="page narrow-page">
      <div className="page-title">
        <p className="eyebrow">Thanh toan</p>
        <h2>Gio hang cua ban</h2>
      </div>
      <CartPanel cart={cart} total={total} onRemove={onRemove} onSubmit={onSubmit} />
      <div className="checkout-summary">
        <span>Tong gia tri</span>
        <strong>{formatCurrency(total)}</strong>
      </div>
    </section>
  );
}
