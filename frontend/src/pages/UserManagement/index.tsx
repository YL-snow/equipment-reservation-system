import { useState, useEffect } from 'react';
import { Table, Button, message, Card, Typography, Tag, Modal, Space } from 'antd';
import dayjs from 'dayjs';
import api from '../../api/api';
import type { UserInfo, PageResult } from '../../types';

const { Title } = Typography;

export default function UserManagement() {
  const [list, setList] = useState<UserInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [blacklistModalVisible, setBlacklistModalVisible] = useState(false);
  const [blacklistLoading, setBlacklistLoading] = useState(false);
  const [selectedUser, setSelectedUser] = useState<UserInfo | null>(null);
  const [searchName, setSearchName] = useState('');
  const [searchUserId, setSearchUserId] = useState('');

  const fetchList = () => {
    setLoading(true);
    api.get<{ data: PageResult<UserInfo> }>('/api/users', {
      params: { 
        page: currentPage, 
        size: pageSize,
        name: searchName || undefined,
        userId: searchUserId || undefined,
      },
    })
      .then((res) => {
        setList(res.data.data.list);
        setTotal(res.data.data.total);
      })
      .catch(() => {
        setList([]);
        setTotal(0);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchList();
    }, 300);
    return () => clearTimeout(timer);
  }, [currentPage, pageSize, searchName, searchUserId]);

  const handleResetPassword = async (id: number) => {
    try {
      const res = await api.put(`/api/users/${id}/reset-password`);
      message.success(res.data?.message || '密码已重置');
      fetchList();
    } catch (err: any) {
      message.error(err?.response?.data?.message || '重置失败');
    }
  };

  const handleBlacklistClick = (user: UserInfo) => {
    setSelectedUser(user);
    setBlacklistModalVisible(true);
  };

  const handleBlacklistConfirm = async () => {
    if (!selectedUser) return;
    setBlacklistLoading(true);
    try {
      const newStatus = !selectedUser.isBlacklisted;
      await api.put(`/api/users/${selectedUser.id}/blacklist`, { isBlacklisted: newStatus });
      message.success(newStatus ? '已加入失信名单' : '已移出失信名单');
      setBlacklistModalVisible(false);
      setSelectedUser(null);
      fetchList();
    } catch (err: any) {
      message.error(err?.response?.data?.message || '操作失败');
    } finally {
      setBlacklistLoading(false);
    }
  };

  const columns = [
    {
      title: '学号',
      dataIndex: 'userId',
      key: 'userId',
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      render: (role: string) => (
        <Tag color={role === 'ADMIN' ? 'blue' : 'green'}>
          {role === 'ADMIN' ? '管理员' : '学生'}
        </Tag>
      ),
    },
    {
      title: '失信状态',
      dataIndex: 'isBlacklisted',
      key: 'isBlacklisted',
      render: (isBlacklisted: boolean, record: UserInfo) => {
        if (isBlacklisted) {
          return (
            <Tag color="red">
              失信人员 {record.blacklistedUntil ? `(至 ${dayjs(record.blacklistedUntil).format('YYYY-MM-DD')})` : ''}
            </Tag>
          );
        }
        return <Tag color="green">正常</Tag>;
      },
    },
    {
      title: '逾期次数',
      dataIndex: 'overdueCount',
      key: 'overdueCount',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: UserInfo) => (
        <Space>
          <Button
            size="small"
            danger={record.isBlacklisted}
            type={record.isBlacklisted ? 'primary' : 'default'}
            onClick={() => handleBlacklistClick(record)}
            disabled={record.role === 'ADMIN'}
          >
            {record.isBlacklisted ? '移出失信' : '加入失信'}
          </Button>
          <Button
            size="small"
            onClick={() => handleResetPassword(record.id)}
            disabled={record.role === 'ADMIN'}
          >
            重置密码
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Card>
      <Title level={4} style={{ marginBottom: 16 }}>用户管理</Title>
      
      <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
        <input
          placeholder="搜索姓名"
          value={searchName}
          onChange={(e) => setSearchName(e.target.value)}
          style={{ width: 200, height: 32, border: '1px solid #d9d9d9', borderRadius: 6, padding: '0 11px' }}
        />
        <input
          placeholder="搜索学号"
          value={searchUserId}
          onChange={(e) => setSearchUserId(e.target.value)}
          style={{ width: 200, height: 32, border: '1px solid #d9d9d9', borderRadius: 6, padding: '0 11px' }}
        />
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={list}
        loading={loading}
        pagination={{
          current: currentPage,
          pageSize,
          total,
          onChange: (page, size) => {
            setCurrentPage(page);
            setPageSize(size);
          },
        }}
      />

      <Modal
        title="失信状态管理"
        open={blacklistModalVisible}
        onOk={handleBlacklistConfirm}
        onCancel={() => {
          setBlacklistModalVisible(false);
          setSelectedUser(null);
        }}
        confirmLoading={blacklistLoading}
        okText={selectedUser?.isBlacklisted ? '移出失信名单' : '加入失信名单'}
        okType={selectedUser?.isBlacklisted ? 'default' : 'primary'}
        okButtonProps={{ danger: !selectedUser?.isBlacklisted }}
        cancelText="取消"
      >
        {selectedUser && (
          <div style={{ textAlign: 'center', padding: '20px 0' }}>
            <p style={{ fontSize: 16, marginBottom: 16 }}>
              用户：<strong>{selectedUser.name}</strong>（{selectedUser.userId}）
            </p>
            <p style={{ marginBottom: 16 }}>
              当前状态：
              {selectedUser.isBlacklisted ? (
                <Tag color="red">失信人员</Tag>
              ) : (
                <Tag color="green">正常</Tag>
              )}
            </p>
            <p>
              {selectedUser.isBlacklisted
                ? '确认将该用户移出失信名单？'
                : '确认将该用户加入失信名单？（有效期 30 天）'}
            </p>
          </div>
        )}
      </Modal>
    </Card>
  );
}
