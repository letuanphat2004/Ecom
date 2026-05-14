import { Trash2 } from 'lucide-react';
import { formatCurrency } from '../utils/formatCurrency.js';

export default function CartPanel({ cart, total, onRemove, onSubmit }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Gio hang</h2>
        <span>{cart.length} mon</span>
      </div>
      <div className="list">
        {cart.map((item) => (
          <div className="line-item" key={item.id}>
            <div>
              <strong>{item.name}</strong>
              <span>
                {item.quantity} x {formatCurrency(item.price)}
              </span>
            </div>
            <button className="icon-button danger" title="Xoa" onClick={() => onRemove(item.id)}>
              <Trash2 size={16} />
            </button>
          </div>
        ))}
        {cart.length === 0 && <p className="muted">Chua co san pham.</p>}
      </div>
      <div className="total-row">
        <span>Tong</span>
        <strong>{formatCurrency(total)}</strong>
      </div>
      <button onClick={onSubmit}>Dat hang</button>
    </section>
  );
}
