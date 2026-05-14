import { useEffect, useState } from 'react';
import { apiRequest } from '../services/api.js';

export default function AdminInventoryPage({ token, onMessage }) {
  const [stock, setStock] = useState([]);
  const [movements, setMovements] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState('');
  const [quantity, setQuantity] = useState('');
  const [stockQuantity, setStockQuantity] = useState('');
  const [reason, setReason] = useState('');

  useEffect(() => {
    loadInventory();
  }, []);

  async function loadInventory() {
    try {
      const [stockResponse, movementResponse] = await Promise.all([
        apiRequest('/inventory', {}, token),
        apiRequest('/inventory/movements', {}, token),
      ]);
      setStock(stockResponse);
      setMovements(movementResponse);
      if (!selectedProductId && stockResponse.length > 0) {
        setSelectedProductId(String(stockResponse[0].productId));
      }
    } catch (error) {
      onMessage(error.message);
    }
  }

  async function restock(event) {
    event.preventDefault();
    if (!selectedProductId) return;

    try {
      await apiRequest(
        `/inventory/${selectedProductId}/restock`,
        {
          method: 'PATCH',
          body: JSON.stringify({ quantity: Number(quantity), reason }),
        },
        token,
      );
      setQuantity('');
      setReason('');
      onMessage('Da nhap them hang.');
      await loadInventory();
    } catch (error) {
      onMessage(error.message);
    }
  }

  async function adjustStock(event) {
    event.preventDefault();
    if (!selectedProductId) return;

    try {
      await apiRequest(
        `/inventory/${selectedProductId}/adjust`,
        {
          method: 'PATCH',
          body: JSON.stringify({ stockQuantity: Number(stockQuantity), reason }),
        },
        token,
      );
      setStockQuantity('');
      setReason('');
      onMessage('Da dieu chinh ton kho.');
      await loadInventory();
    } catch (error) {
      onMessage(error.message);
    }
  }

  return (
    <section className="page admin-page">
      <div className="page-title">
        <div>
          <p className="eyebrow">Admin</p>
          <h2>Quan ly kho hang</h2>
        </div>
        <button onClick={loadInventory}>Tai lai</button>
      </div>

      <div className="admin-grid">
        <section className="panel">
          <div className="panel-header">
            <h2>Ton kho</h2>
            <span>{stock.length} san pham</span>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>San pham</th>
                  <th>Ton kho</th>
                  <th>Trang thai</th>
                </tr>
              </thead>
              <tbody>
                {stock.map((item) => (
                  <tr key={item.productId}>
                    <td>{item.productName}</td>
                    <td>{item.stockQuantity}</td>
                    <td>{item.active ? 'Dang ban' : 'Tam an'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel">
          <div className="panel-header">
            <h2>Cap nhat kho</h2>
          </div>
          <label className="field-label">
            San pham
            <select value={selectedProductId} onChange={(event) => setSelectedProductId(event.target.value)}>
              {stock.map((item) => (
                <option key={item.productId} value={item.productId}>
                  {item.productName}
                </option>
              ))}
            </select>
          </label>

          <form className="form inventory-form" onSubmit={restock}>
            <input
              value={quantity}
              min="1"
              onChange={(event) => setQuantity(event.target.value)}
              placeholder="So luong nhap them"
              type="number"
              required
            />
            <input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Ly do" />
            <button type="submit">Nhap them hang</button>
          </form>

          <form className="form inventory-form" onSubmit={adjustStock}>
            <input
              value={stockQuantity}
              min="0"
              onChange={(event) => setStockQuantity(event.target.value)}
              placeholder="Dat lai ton kho"
              type="number"
              required
            />
            <input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Ly do" />
            <button type="submit">Dieu chinh ton</button>
          </form>
        </section>
      </div>

      <section className="panel movement-panel">
        <div className="panel-header">
          <h2>Lich su kho</h2>
          <span>{movements.length} giao dich gan nhat</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>San pham</th>
                <th>Loai</th>
                <th>Thay doi</th>
                <th>Ton sau</th>
                <th>Nguoi tao</th>
                <th>Ly do</th>
              </tr>
            </thead>
            <tbody>
              {movements.map((movement) => (
                <tr key={movement.id}>
                  <td>{movement.productName}</td>
                  <td>{movement.type}</td>
                  <td>{movement.quantityChange}</td>
                  <td>{movement.stockAfter}</td>
                  <td>{movement.createdBy}</td>
                  <td>{movement.reason || '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}
