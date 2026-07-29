import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface AuthGuardProps {
  children?: React.ReactNode;
  roles?: string[];
}

const AuthGuard: React.FC<AuthGuardProps> = ({ children, roles }) => {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (roles && roles.length > 0) {
    const userRole = user?.role || '';
    if (!roles.includes(userRole)) {
      return <Navigate to="/" />;
    }
  }

  if (children) {
    return <>{children}</>;
  }

  return <Outlet />;
};

export default AuthGuard;
