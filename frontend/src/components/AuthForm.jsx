import { useState } from 'react';

export default function AuthForm({ mode, onSubmit, submitLabel }) {
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const isRegister = mode === 'register';

  function updateField(field, value) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  function submit(event) {
    event.preventDefault();
    onSubmit(form);
  }

  return (
    <form className="form" onSubmit={submit}>
      {isRegister && (
        <input
          value={form.fullName}
          onChange={(event) => updateField('fullName', event.target.value)}
          placeholder="Ho ten"
          required
        />
      )}
      <input
        value={form.email}
        onChange={(event) => updateField('email', event.target.value)}
        placeholder="Email"
        type="email"
        required
      />
      <input
        value={form.password}
        onChange={(event) => updateField('password', event.target.value)}
        placeholder="Mat khau"
        type="password"
        minLength={6}
        required
      />
      <button type="submit">{submitLabel}</button>
    </form>
  );
}
