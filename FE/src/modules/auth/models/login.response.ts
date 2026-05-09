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
  menus?: Menu[];
  homeURL?: string;
}

