import { useEffect, useState, useCallback } from 'react';
import { Table, Tag, Button, Space, message, Card, Typography } from 'antd';
import dayjs from 'dayjs';
import api from '../../api/api';
import type { Reservation, ReservationStatus } from '../../types';

const { Title } = Typography;

const statusColors: Record<ReservationStatus, string> = {
  PENDING: 'orange',
  APPROVED: 'green',
  REJECTED: 'red',
  RETURNED: 'blue',
  CANCELLED: 'default',
};

const statusLabels: Record<ReservationStatus, string> = {
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  RETURNED: '已归还',
  CANCELLED: '已取消',
};

export default function Approval() {
  const [list, setList] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [operating, setOperating] = useState<number | null>(null);

  const fetchList = useCallback(() => {
    setLoading(true);
    api.get<{ data: Reservation[] }>('/api/reservations')
      .then((res) => setList(res.data.data))
      .catch(() => setList([]))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    fetchList();
  }, [fetchList]);

  const handleApprove = async (id: number) => {
    // 二次校验：从当前列表中获取最新状态
    const record = list.find((r) => r.id === id);
    if (record?.status !== 'PENDING') {
      message.warning('该预约状态已变更，请刷新列表');
      return;
    }

    setOperating(id);
    try {
      await api.put(`/api/reservations/${id}/approve`, { operator: '管理员' });
      message.success('审批通过');
      await fetchList();
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || '操作失败';
      message.error(msg);
      console.error('操作失败详情:', err);
    } finally {
      setOperating(null);
    }
  };

  const handleReject = async (id: number) => {
    const record = list.find((r) => r.id === id);
    if (record?.status !== 'PENDING') {
      message.warning('该预约状态已变更，请刷新列表');
      return;
    }

    setOperating(id);
    try {
      await api.put(`/api/reservations/${id}/reject`, { operator: '管理员' });
      message.success('已驳回');
      await fetchList();
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || '操作失败';
      message.error(msg);
      console.error('操作失败详情:', err);
    } finally {
      setOperating(null);
    }
  };

  const handleReturn = async (id: number) => {
    const record = list.find((r) => r.id === id);
    if (record?.status !== 'APPROVED') {
      message.warning('仅已通过的预约可以归还');
      return;
    }

    setOperating(id);
    try {
      await api.put(`/api/reservations/${id}/return`, { operator: '管理员' });
      message.success('归还成功');
      await fetchList();
    } catch (err: any) {
      const msg = err?.response?.data?.message || err?.message || '操作失败';
      message.error(msg);
      console.error('操作失败详情:', err);
    } finally {
      setOperating(null);
    }
  };

  const columns = [
    {
      title: '设备',
      dataIndex: ['equipment', 'name'],
      key: 'equipment',
    },
    {
      title: '申请人',
      dataIndex: 'applicant',
      key: 'applicant',
    },
    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
    },
    {
      title: '时间',
      key: 'time',
      render: (_: unknown, r: Reservation) =>
        `${dayjs(r.startTime).format('YYYY-MM-DD HH:mm:ss')} ~ ${dayjs(r.endTime).format('YYYY-MM-DD HH:mm:ss')}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (s: ReservationStatus) => (
        <Tag color={statusColors[s]}>{statusLabels[s]}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, r: Reservation) => {
        if (r.status === 'PENDING') {
          return (
            <Space>
              <Button
                type="primary"
                size="small"
                loading={operating === r.id}
                onClick={() => handleApprove(r.id)}
              >
                通过
              </Button>
              <Button
                danger
                size="small"
                loading={operating === r.id}
                onClick={() => handleReject(r.id)}
              >
                驳回
              </Button>
            </Space>
          );
        }
        if (r.status === 'APPROVED') {
          return (
            <Button
              type="primary"
              size="small"
              loading={operating === r.id}
              onClick={() => handleReturn(r.id)}
            >
              归还
            </Button>
          );
        }
        return '--';
      },
    },
  ];

  return (
    <Card>
      <Title level={4}>审批管理</Title>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={list}
        loading={loading}
        pagination={{ pageSize: 10 }}
      />
    </Card>
  );
}
