import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 8178,
  login: 'qZ&Rg@yQ\\*5pB',
};

export const sampleWithPartialData: IUser = {
  id: 5777,
  login: 'fp',
};

export const sampleWithFullData: IUser = {
  id: 31120,
  login: 'w@q\\!925e-\\erPVmm',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
