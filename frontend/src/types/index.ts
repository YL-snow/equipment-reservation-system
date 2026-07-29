export type ReservationStatus =
  | 'PENDING'
  | 'APPROVED'
  | 'REJECTED'
  | 'RETURNED'
  | 'CANCELLED';

export interface ApiResult<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
}

export interface Equipment {
  id: number;
  name: string;
  model: string;
  totalQuantity: number;
  availableQty: number;
  status: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface Reservation {
  id: number;
  equipment: Equipment;
  applicant: string;
  quantity: number;
  startTime: string;
  endTime: string;
  status: ReservationStatus;
  remark: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ConflictCheckResponse {
  conflict: boolean;
  conflictReservations: Array<{
    id: number;
    applicant: string;
    startTime: string;
    endTime: string;
  }>;
}

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface RegisterRequest {
  userId: string;
  name: string;
  password: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface UserInfo {
  id: number;
  userId: string;
  name: string;
  role: string;
  isBlacklisted: boolean;
  blacklistedUntil: string | null;
  overdueCount: number;
}

export interface LoginResponse {
  id: number;
  userId: string;
  name: string;
  role: string;
  token: string;
  isBlacklisted: boolean;
  blacklistedUntil: string | null;
}

export interface EquipmentDTO extends Equipment {
  categoryId: number | null;
  categoryName: string | null;
}

export interface EquipmentCategory {
  id: number;
  name: string;
  description: string | null;
}

export interface PageResult<T> {
  list: T[];
  total: number;
}
