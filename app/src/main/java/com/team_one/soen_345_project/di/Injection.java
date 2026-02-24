package com.team_one.soen_345_project.di;

import com.team_one.soen_345_project.model.repository.IAuthRepository;
import com.team_one.soen_345_project.model.repository.IEventRepository;
import com.team_one.soen_345_project.model.repository.impl.FirebaseAuthRepository;
import com.team_one.soen_345_project.model.repository.impl.FirebaseEventRepository;

/**
 * Service Locator / Dependency Provider.
 * Acts as the "Glue" that connects interfaces to implementations.
 */
public class Injection {
    /**
     * Provides a concrete implementation of IAuthRepository.
     * By centralizing this 'new' call, we prevent the ViewModel
     * from knowing which specific database (Firebase, SQL, etc.) is being used.
     */
    public static IAuthRepository provideAuthRepository() {
        return new FirebaseAuthRepository();
    }

    public static IEventRepository provideEventRepository() {
        return new FirebaseEventRepository();
    }
}
