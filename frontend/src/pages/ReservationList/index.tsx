import { useEffect, useState } from 'react';
import { Table, Tag, Select, Card, Typography } from 'antd';
import dayjs from 'dayjs';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
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

export default function ReservationList() {
  const [list, setList] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState<string | undefined>();
  const { isAdmin } = useAuth();

  const fetchList = () => {
    setLoading(true);
    api.get<{ data: Reservation[] }>('/api/reservations')
      .then((res) => setList(res.data.data))
      .catch(() => setList([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchList();
  }, []);

  const filtered = statusFilter
    ? list.filter((r) => r.status === statusFilter)
    : list;

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
      title: '开始时间',
      dataIndex: 'startTime',
      key: 'startTime',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '结束时间',
      dataIndex: 'endTime',
      key: 'endTime',
      render: (v: string) => dayjs(v).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (s: ReservationStatus) => (
        <Tag color={statusColors[s]}>{statusLabels[s]}</Tag>
      ),
    },
  ];

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <Title level={4} style={{ margin: 0 }}>{isAdmin ? '所有预约记录' : '我的预约记录'}</Title>
          <Select
            allowClear
            placeholder="按状态筛选"
            style={{ width: 150 }}
            value={statusFilter}
            onChange={setStatusFilter}
            options={Object.entries(statusLabels).map(([value, label]) => ({ value, label }))}
          />
        </div>
        <Table
          rowKey="id"
          columns={columns}
          dataSource={filtered}
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </div>
  );
}
