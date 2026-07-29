import { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Form, Select, InputNumber, DatePicker, Button, Alert, message, Card, Typography, Input,
} from 'antd';
import dayjs from 'dayjs';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import type { EquipmentDTO, ConflictCheckResponse } from '../../types';

const { Text } = Typography;

export default function ReservationCreate() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const { user } = useAuth();

  const [equipments, setEquipments] = useState<EquipmentDTO[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [conflict, setConflict] = useState<ConflictCheckResponse | null>(null);
  const [checking, setChecking] = useState(false);
  const timerRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  useEffect(() => {
    api.get<{ data: EquipmentDTO[] }>('/api/equipment')
      .then((res) => setEquipments(res.data.data));
    
    const eqId = searchParams.get('equipmentId');
    if (eqId) {
      form.setFieldsValue({ equipmentId: Number(eqId) });
    }
    
    if (user) {
      form.setFieldsValue({ applicant: user.name });
    }
  }, [searchParams, form, user]);

  const equipmentId = Form.useWatch('equipmentId', form);
  const startTime = Form.useWatch('startTime', form);
  const endTime = Form.useWatch('endTime', form);

  const checkConflict = useCallback(async (eqId: number, st: dayjs.Dayjs, et: dayjs.Dayjs) => {
    if (!eqId || !st || !et) {
      setConflict(null);
      return;
    }
    setChecking(true);
    try {
      const res = await api.get<{ data: ConflictCheckResponse }>('/api/reservations/conflict-check', {
        params: {
          equipmentId: eqId,
          startTime: st.format('YYYY-MM-DDTHH:mm:ss'),
          endTime: et.format('YYYY-MM-DDTHH:mm:ss'),
        },
      });
      setConflict(res.data.data);
    } catch {
      setConflict(null);
    } finally {
      setChecking(false);
    }
  }, []);

  useEffect(() => {
    if (timerRef.current) clearTimeout(timerRef.current);
    if (equipmentId && startTime && endTime) {
      timerRef.current = setTimeout(() => {
        checkConflict(equipmentId, startTime, endTime);
      }, 500);
    } else {
      setConflict(null);
    }
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, [equipmentId, startTime, endTime, checkConflict]);

  const onFinish = async (values: {
    equipmentId: number;
    quantity: number;
    startTime: dayjs.Dayjs;
    endTime: dayjs.Dayjs;
    remark?: string;
  }) => {
    setSubmitting(true);
    try {
      await api.post('/api/reservations', {
        equipmentId: values.equipmentId,
        applicant: user?.name || '',
        userId: user?.id,
        quantity: values.quantity,
        startTime: values.startTime.format('YYYY-MM-DDTHH:mm:ss'),
        endTime: values.endTime.format('YYYY-MM-DDTHH:mm:ss'),
        remark: values.remark ?? '',
      });
      message.success('预约提交成功');
      navigate('/reservations');
    } catch (err: any) {
      const msg = err?.response?.data?.message ?? '提交失败';
      message.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Card title="新建预约" style={{ maxWidth: 600, margin: '0 auto' }}>
      <Form form={form} layout="vertical" onFinish={onFinish}>
        <Form.Item name="equipmentId" label="设备" rules={[{ required: true, message: '请选择设备' }]}>
          <Select
            placeholder="请选择设备"
            options={equipments.map((e) => ({ 
              label: `${e.name} (${e.model})${e.categoryName ? ` - ${e.categoryName}` : ''}`, 
              value: e.id 
            }))}
          />
        </Form.Item>

        <Form.Item label="申请人" style={{ marginBottom: 0 }}>
          <Text type="secondary">{user?.name || '未登录'}</Text>
        </Form.Item>

        <Form.Item name="quantity" label="预约数量" rules={[{ required: true, message: '请输入数量' }]}>
          <InputNumber min={1} style={{ width: '100%' }} placeholder="请输入数量" />
        </Form.Item>

        <Form.Item
          name="startTime"
          label="开始时间"
          rules={[{ required: true, message: '请选择开始时间' }]}
        >
          <DatePicker
            showTime
            format="YYYY-MM-DDTHH:mm:ss"
            style={{ width: '100%' }}
            placeholder="请选择开始时间"
            disabledDate={(current) => current && current < dayjs().startOf('day')}
          />
        </Form.Item>

        <Form.Item
          name="endTime"
          label="结束时间"
          rules={[{ required: true, message: '请选择结束时间' }]}
        >
          <DatePicker
            showTime
            format="YYYY-MM-DDTHH:mm:ss"
            style={{ width: '100%' }}
            placeholder="请选择结束时间"
            disabledDate={(current) => {
              const st = form.getFieldValue('startTime');
              if (st && current) {
                return current < st;
              }
              return current && current < dayjs().startOf('day');
            }}
          />
        </Form.Item>

        {checking && <Text type="secondary">检查中...</Text>}
        {!checking && conflict && (
          conflict.conflict
            ? <Alert type="error" message="该时段已被占用" style={{ marginBottom: 16 }} showIcon />
            : <Alert type="success" message="该时段可预约" style={{ marginBottom: 16 }} showIcon />
        )}

        <Form.Item name="remark" label="备注">
          <Input.TextArea rows={3} placeholder="备注（可选）" />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={submitting} block>
            提交预约
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
}
