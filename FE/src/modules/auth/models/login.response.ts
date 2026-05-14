export interface Menu {
  id: string;
  name: string;
  urlink: string;
  classlink: string;
  sortOrder: string;
  icon: string;
  typeMenu: number;
  code: string;
  parentId: string | null;
}

export interface LoginResponse {
  id: string;
  sessionToken: string;
  role: 'USER' | 'ADMIN';
  fullName?: string;
  menus?: Menu[];
  homeURL?: string;
}

