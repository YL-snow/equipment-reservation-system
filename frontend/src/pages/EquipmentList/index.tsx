import { useEffect, useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Col, Row, Tag, Button, Spin, Typography, Space, Input, Select } from 'antd';
import api from '../../api/api';
import type { EquipmentDTO, EquipmentCategory } from '../../types';

const { Text } = Typography;

export default function EquipmentList() {
  const [allEquipments, setAllEquipments] = useState<EquipmentDTO[]>([]);
  const [categories, setCategories] = useState<EquipmentCategory[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchName, setSearchName] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>();
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    api.get<{ data: EquipmentDTO[] }>('/api/equipment')
      .then((res) => setAllEquipments(res.data.data))
      .catch(() => setAllEquipments([]))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    api.get<{ data: EquipmentCategory[] }>('/api/equipment/categories')
      .then((res) => setCategories(res.data.data));
  }, []);

  const filteredList = useMemo(() => {
    if (searchName) {
      return allEquipments.filter((eq) =>
        eq.name.toLowerCase().includes(searchName.toLowerCase())
      );
    }
    if (selectedCategory !== undefined && selectedCategory !== 0) {
      return allEquipments.filter((eq) => eq.categoryId === selectedCategory);
    }
    return allEquipments;
  }, [allEquipments, searchName, selectedCategory]);

  if (loading) {
    return <Spin size="large" style={{ display: 'block', marginTop: 100 }} />;
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
        <Input
          placeholder="搜索设备名称"
          value={searchName}
          onChange={(e) => setSearchName(e.target.value)}
          style={{ width: 250 }}
        />
        <Select
          placeholder="选择分类"
          allowClear
          value={selectedCategory}
          onChange={setSelectedCategory}
          style={{ width: 200 }}
          options={[
            { label: '全部设备', value: 0 },
            ...categories.map((c) => ({ label: c.name, value: c.id })),
          ]}
        />
      </div>
      <Row gutter={[16, 16]}>
        {filteredList.map((eq) => (
          <Col key={eq.id} xs={24} sm={12} md={8} lg={6}>
            <Card
              title={eq.name}
              actions={[
                <Button
                  type="primary"
                  onClick={() => navigate(`/reservations/new?equipmentId=${eq.id}`)}
                >
                  预约
                </Button>,
              ]}
            >
              <Space orientation="vertical" size={4}>
                <Text>型号：{eq.model}</Text>
                {eq.categoryName && <Text>分类：{eq.categoryName}</Text>}
                <Text>总库存：{eq.totalQuantity}</Text>
                <Text>
                  可用库存：{eq.availableQty}
                  {eq.availableQty < 5 && (
                    <Tag color="red" style={{ marginLeft: 8 }}>库存预警</Tag>
                  )}
                </Text>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}