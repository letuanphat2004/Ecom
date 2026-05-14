import { PackagePlus, ShoppingCart } from 'lucide-react';
import { formatCurrency } from '../utils/formatCurrency.js';

export default function ProductCard({ product, onAddToCart }) {
  return (
    <article className="product-card">
      <div className="product-media">
        {product.imageUrl ? <img src={product.imageUrl} alt={product.name} /> : <PackagePlus size={42} />}
      </div>
      <div className="product-body">
        <h3>{product.name}</h3>
        <p>{product.description || 'Chua co mo ta.'}</p>
        <div className="product-meta">
          <strong>{formatCurrency(product.price)}</strong>
          <span>Ton kho: {product.stockQuantity}</span>
        </div>
        <button onClick={() => onAddToCart(product)}>
          <ShoppingCart size={17} />
          Them vao gio
        </button>
      </div>
    </article>
  );
}
