package com.atlassian.interviews.uag.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * A user that may or may not belong to any groups.
 */
@ParametersAreNonnullByDefault
public class User implements Comparable<User> {
    private final String name;

    /**
     * Creates a new instance of a user.
     * This does not implicitly register the user with the {@link UserService}.
     *
     * @param name the unique name that identifies this user; must not be {@code null}.
     */
    public User(String name) {
        this.name = requireNonNull(name, "name");
    }

    /**
     * Returns the name that identifies this user.
     *
     * @return the name that identifies this user.
     */
    public String getName() {
        return name;
    }

    public int compareTo(@Nonnull User other) {
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof User) {
            User user = (User) object;
            return name.equals(user.name);
        }
        return false;
    }
}
