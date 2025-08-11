export interface Cat {
  id: number;
  name: string;
  age: number;
  gender: 'MALE' | 'FEMALE';
  description: string;
  breedId: string;
  breedName: string;
  imageUrl?: string;
  latitude: number;
  longitude: number;
  address: string;
  adoptionStatus: 'AVAILABLE' | 'PENDING' | 'ADOPTED';
  createdAt: string;
  updatedAt: string;
}

export interface CatBreed {
  id: string;
  name: string;
  description: string;
  origin: string;
  childFriendly: number;
  dogFriendly: number;
  energyLevel: number;
  grooming: number;
  healthIssues: number;
  intelligence: number;
  socialNeeds: number;
  strangerFriendly: number;
  adaptability: number;
  affectionLevel: number;
  wikipediaUrl: string;
  referenceImageId: string
  imageUrl: string;
}

export interface AdoptionApplication {
  id?: number;
  status: 'PENDING' | 'APPROVED' | 'COMPLETED' | 'REJECTED';
  adoptionDate?: string;
  approvedDate?: string;
  completedDate?: string;
  notes?: string;
  adminNotes?: string;
  tenantId?: string;
  user: {
    id: number;
    fullName: string;
    email: string;
    phone?: string;
  };
  cat: {
    id: number;
    name: string;
    breed: string;
  };
}

export interface CreateCatRequest {
  name: string;
  age: number;
  gender: 'MALE' | 'FEMALE';
  description: string;
  breedId: string;
  breedName: string;
  imageUrl?: string | null;
  latitude: number;
  longitude:number;
  address: string;
  status: 'AVAILABLE';
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
