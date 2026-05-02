export interface BaseResponse<T> {
  messages: string[];
  payload: T;
  status: number;
}
