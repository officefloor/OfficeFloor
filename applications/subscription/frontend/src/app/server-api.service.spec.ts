import { TestBed } from '@angular/core/testing';

import { ServerApiService } from './server-api.service';

describe('ServerApiService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: ServerApiService = TestBed.get(ServerApiService);
    expect(service).toBeTruthy();
  });
});
