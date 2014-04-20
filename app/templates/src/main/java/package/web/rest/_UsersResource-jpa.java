package <%=packageName%>.web.rest;

import <%=packageName%>.domain.User;
import <%=packageName%>.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 *
 */
@RestController
@RequestMapping("/api/v1/users")
public class UsersResource extends AbstractRestResource<User, UUID, User.UserWrapper> {
    @Autowired
    private UserRepository userRepository;

    @Override
    protected Class<User> entityClass() {
        return User.class;
    }

    @Override
    protected PagingAndSortingRepository<User, UUID> repository() {
        return userRepository;
    }

    @Override
    protected User.UserWrapper entityWrapper(User entity) {
        return new User.UserWrapper(entity);
    }
}
