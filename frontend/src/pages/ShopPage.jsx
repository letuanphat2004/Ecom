import { useMemo, useState } from 'react';
import { Search } from 'lucide-react';
import ProductCard from '../components/ProductCard.jsx';

export default function ShopPage({ products, onAddToCart }) {
  const [search, setSearch] = useState('');

  const filteredProducts = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    if (!keyword) return products;
    return products.filter((product) =>
      `${product.name} ${product.description || ''}`.toLowerCase().includes(keyword),
    );
  }, [products, search]);

  return (
    <section className="page">
      <div className="catalog-toolbar">
        <div>
          <p className="eyebrow">San pham</p>
          <h2>Danh sach dang ban</h2>
        </div>
        <label className="search-box">
          <Search size={18} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Tim san pham" />
        </label>
      </div>

      <div className="product-grid">
        {filteredProducts.map((product) => (
          <ProductCard key={product.id} product={product} onAddToCart={onAddToCart} />
        ))}
        {filteredProducts.length === 0 && <p className="empty">Chua co san pham nao trong he thong.</p>}
      </div>
    </section>
  );
}
