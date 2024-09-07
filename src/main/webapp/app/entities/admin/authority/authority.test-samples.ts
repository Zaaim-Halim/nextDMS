import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: '72b4462e-3d8b-4825-8c05-47e1fc771281',
};

export const sampleWithPartialData: IAuthority = {
  name: '3de31257-bea4-40f5-a73f-454922aeda47',
};

export const sampleWithFullData: IAuthority = {
  name: 'f9cf36a6-2eee-4041-bdef-bfab354b90f9',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
