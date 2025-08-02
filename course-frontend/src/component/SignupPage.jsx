import React, { useState } from 'react';
import instance from '../component/AxiosInstance';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import '../Styles/LoginPage.css'; // Using same CSS as login

const SignupPage = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSignup = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post('http://localhost:8080/auth/signup', {
        name, email, password,
      });
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('userId', res.data.id);
      navigate('/');
    } catch (err) {
      setError('Signup failed. Try again.');
    }
  };

  return (
    <div className="login-container">
      <div className="login-box">
        <h2 className="login-title">Sign Up</h2>
        {error && <p className="error-msg">{error}</p>}
        <form onSubmit={handleSignup}>
         <input
  type="text"
  className="login-input"
  value={name}
  onChange={e => setName(e.target.value)}
  placeholder="Name"
  required
/>

          <input
            type="email"
            className="login-input"
            value={email}
            onChange={e => setEmail(e.target.value)}
            placeholder="Email"
            required
          />
          <input
            type="password"
            className="login-input"
            value={password}
            onChange={e => setPassword(e.target.value)}
            placeholder="Password"
            required
          />
          <button type="submit" className="login-button">Sign Up</button>
        </form>
        <p className="signup-text">
          Already have an account?{' '}
          <span className="signup-link" onClick={() => navigate('/login')}>Login</span>
        </p>
      </div>
    </div>
  );
};

export default SignupPage;
