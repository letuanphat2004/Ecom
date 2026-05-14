import { formatCurrency } from '../utils/formatCurrency.js';

export default function OrdersPanel({ orders }) {
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Don hang</h2>
        <span>{orders.length}</span>
      </div>
      <div className="list">
        {orders.slice(0, 4).map((order) => (
          <div className="order-row" key={order.id}>
            <strong>
              #{order.id} - {order.status}
            </strong>
            <span>{formatCurrency(order.totalAmount)}</span>
          </div>
        ))}
        {orders.length === 0 && <p className="muted">Dang nhap de xem don hang.</p>}
      </div>
    </section>
  );
}
