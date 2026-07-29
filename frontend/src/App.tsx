
import { Routes, Route, Link, useLocation, Navigate, Outlet } from 'react-router-dom';
import { Layout, Menu, Button } from 'antd';
import { LogoutOutlined, UserOutlined } from '@ant-design/icons';
import EquipmentList from './pages/EquipmentList';
import ReservationCreate from './pages/ReservationCreate';
import ReservationList from './pages/ReservationList';
import Approval from './pages/Approval';
import Login from './pages/Login';
import Register from './pages/Register';
import UserManagement from './pages/UserManagement';
import AuthGuard from './components/AuthGuard';
import { useAuth } from './context/AuthContext';

const { Header, Content } = Layout;

function MainLayout() {
  const location = useLocation();
  const { user, logout, isAuthenticated, isAdmin } = useAuth();

  const studentMenuItems = [
    { key: '/', label: <Link to="/">设备列表</Link> },
    { key: '/reservations', label: <Link to="/reservations">预约记录</Link> },
    { key: '/reservations/new', label: <Link to="/reservations/new">新建预约</Link> },
  ];

  const adminMenuItems = [
    { key: '/', label: <Link to="/">设备列表</Link> },
    { key: '/reservations', label: <Link to="/reservations">预约记录</Link> },
    { key: '/reservations/new', label: <Link to="/reservations/new">新建预约</Link> },
    { key: '/approval', label: <Link to="/approval">审批管理</Link> },
    { key: '/admin/users', label: <Link to="/admin/users">用户管理</Link> },
  ];

  const menuItems = isAdmin ? adminMenuItems : studentMenuItems;

  return (
    <Layout>
      <Header style={{ display: 'flex', alignItems: 'center' }}>
        <div style={{ color: '#fff', fontSize: 18, fontWeight: 'bold', marginRight: 24, whiteSpace: 'nowrap' }}>
          设备预约系统
        </div>
        <Menu
          theme="dark"
          mode="horizontal"
          selectedKeys={[location.pathname]}
          items={menuItems}
          style={{ flex: 1, minWidth: 0 }}
        />
        {isAuthenticated && (
          <div style={{ display: 'flex', alignItems: 'center', color: '#fff' }}>
            <span style={{ marginRight: 16, display: 'flex', alignItems: 'center' }}>
              <UserOutlined style={{ marginRight: 4 }} />
              {user?.name} ({user?.role === 'ADMIN' ? '管理员' : '学生'})
            </span>
            <Button 
              type="text" 
              icon={<LogoutOutlined />} 
              onClick={logout}
              style={{ color: '#fff' }}
            >
              退出登录
            </Button>
          </div>
        )}
      </Header>
      <Content style={{ padding: 24, minHeight: 'calc(100vh - 64px)' }}>
        <Outlet />
      </Content>
    </Layout>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      
      <Route element={<AuthGuard />}>
        <Route element={<MainLayout />}>
          <Route path="/" element={<EquipmentList />} />
          <Route path="/reservations" element={<ReservationList />} />
          <Route path="/reservations/new" element={<ReservationCreate />} />
          
          <Route path="/approval" element={
            <AuthGuard roles={['ADMIN']}>
              <Approval />
            </AuthGuard>
          } />
          <Route path="/admin/users" element={
            <AuthGuard roles={['ADMIN']}>
              <UserManagement />
            </AuthGuard>
          } />
        </Route>
      </Route>
      
      <Route path="*" element={<Navigate to="/login" />} />
    </Routes>
  );
}

export default App;
