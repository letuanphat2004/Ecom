import { Link, useNavigate } from 'react-router-dom';
import AuthForm from '../components/AuthForm.jsx';
import { apiRequest } from '../services/api.js';

export default function RegisterPage({ onAuth, onMessage }) {
  const navigate = useNavigate();

  async function register(form) {
    try {
      const response = await apiRequest('/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          fullName: form.fullName,
          email: form.email,
          password: form.password,
        }),
      });
      onAuth(response);
      onMessage('Dang ky thanh cong.');
      navigate('/shop', { replace: true });
    } catch (error) {
      onMessage(error.message);
    }
  }

  return (
    <section className="auth-page">
      <div className="auth-card">
        <p className="eyebrow">Tai khoan</p>
        <h2>Dang ky</h2>
        <AuthForm mode="register" onSubmit={register} submitLabel="Dang ky" />
        <p className="auth-switch">
          Da co tai khoan? <Link to="/login">Dang nhap</Link>
        </p>
      </div>
    </section>
  );
}
