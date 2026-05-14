import OrdersPanel from '../components/OrdersPanel.jsx';

export default function OrdersPage({ orders, onRefresh }) {
  return (
    <section className="page narrow-page">
      <div className="page-title">
        <div>
          <p className="eyebrow">Lich su</p>
          <h2>Don hang cua ban</h2>
        </div>
        <button onClick={onRefresh}>Tai lai</button>
      </div>
      <OrdersPanel orders={orders} />
    </section>
  );
}
