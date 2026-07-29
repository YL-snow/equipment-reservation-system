import React, { useState, useEffect } from 'react';
import { Form, Input, Button, message, Card, Progress } from 'antd';
import { UserOutlined, LockOutlined, EyeOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import type { RegisterRequest } from '../../types';

const Register: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordStrength, setPasswordStrength] = useState(0);
  const [passwordStrengthText, setPasswordStrengthText] = useState('');
  const [passwordStrengthColor, setPasswordStrengthColor] = useState('');
  const navigate = useNavigate();
  const { register, isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  if (isAuthenticated) {
    return null;
  }

  useEffect(() => {
    if (!password) {
      setPasswordStrength(0);
      setPasswordStrengthText('');
      setPasswordStrengthColor('');
      return;
    }

    let score = 0;
    if (password.length >= 8) score += 30;
    if (/[a-zA-Z]/.test(password)) score += 35;
    if (/[0-9]/.test(password)) score += 35;
    if (/[!@#$%^&*()_+\-=\[\]{}|;:,.<>?]/.test(password)) score += 20;

    setPasswordStrength(Math.min(score, 100));

    if (score < 60) {
      setPasswordStrengthText('弱');
      setPasswordStrengthColor('#ff4d4f');
    } else if (score < 80) {
      setPasswordStrengthText('中');
      setPasswordStrengthColor('#faad14');
    } else {
      setPasswordStrengthText('强');
      setPasswordStrengthColor('#52c41a');
    }
  }, [password]);

  const validateConfirmPassword = () => {
    if (confirmPassword && password !== confirmPassword) {
      return Promise.reject(new Error('两次输入的密码不一致'));
    }
    return Promise.resolve();
  };

  const validatePassword = () => {
    if (!password) return Promise.resolve();
    if (password.length < 8) {
      return Promise.reject(new Error('密码长度不能少于8位'));
    }
    if (!/[a-zA-Z]/.test(password)) {
      return Promise.reject(new Error('密码必须包含字母'));
    }
    if (!/[0-9]/.test(password)) {
      return Promise.reject(new Error('密码必须包含数字'));
    }
    return Promise.resolve();
  };

  const handleSubmit = async (values: RegisterRequest & { confirmPassword: string }) => {
    setLoading(true);
    try {
      await register({
        userId: values.userId,
        name: values.name,
        password: values.password,
      });
      message.success('注册成功，请登录');
      navigate('/login');
    } catch (error) {
      message.error((error as Error).message || '注册失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: '100vh', 
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' 
    }}>
      <Card 
        style={{ 
          width: 450, 
          boxShadow: '0 10px 40px rgba(0,0,0,0.2)',
          borderRadius: '12px'
        }}
        title={
          <div style={{ textAlign: 'center', fontSize: '24px', fontWeight: 'bold', color: '#1890ff' }}>
            学生注册
          </div>
        }
      >
        <Form
          name="register"
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ userId: '', name: '', password: '', confirmPassword: '' }}
        >
          <Form.Item
            name="userId"
            label="学号"
            rules={[
              { required: true, message: '请输入学号' },
              { max: 50, message: '学号长度不能超过50位' },
            ]}
          >
            <Input 
              prefix={<UserOutlined />} 
              placeholder="请输入学号"
            />
          </Form.Item>

          <Form.Item
            name="name"
            label="姓名"
            rules={[
              { required: true, message: '请输入姓名' },
              { max: 50, message: '姓名长度不能超过50位' },
            ]}
          >
            <Input 
              prefix={<EyeOutlined />} 
              placeholder="请输入姓名"
            />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[
              { required: true, message: '请输入密码' },
              { validator: () => validatePassword() },
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="请输入密码（至少8位，包含字母和数字）"
              onChange={(e) => setPassword(e.target.value)}
            />
          </Form.Item>

          {password && (
            <div style={{ marginBottom: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                <span style={{ fontSize: 12, color: '#666' }}>密码强度</span>
                <span style={{ fontSize: 12, color: passwordStrengthColor, fontWeight: 'bold' }}>
                  {passwordStrengthText}
                </span>
              </div>
              <Progress 
                percent={passwordStrength} 
                strokeColor={passwordStrengthColor}
                size="small"
                showInfo={false}
              />
            </div>
          )}

          <Form.Item
            name="confirmPassword"
            label="确认密码"
            rules={[
              { required: true, message: '请确认密码' },
              { validator: () => validateConfirmPassword() },
            ]}
          >
            <Input.Password 
              prefix={<LockOutlined />} 
              placeholder="请再次输入密码"
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </Form.Item>

          <Form.Item>
            <Button 
              type="primary" 
              htmlType="submit" 
              loading={loading}
              style={{ width: '100%', height: 40, fontSize: 16 }}
            >
              注册
            </Button>
          </Form.Item>

          <Form.Item style={{ textAlign: 'center' }}>
            <Link to="/login">
              已有账号？立即登录
            </Link>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default Register;
