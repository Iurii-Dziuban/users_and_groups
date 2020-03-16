package com.atlassian.interviews.uag.memory;

import com.atlassian.interviews.uag.api.Group;
import com.atlassian.interviews.uag.api.MembershipService;
import com.atlassian.interviews.uag.api.User;
import com.atlassian.interviews.uag.core.AbstractService;
import com.atlassian.interviews.uag.core.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of the membership service that stores user and group relationships in memory.
 */
@ParametersAreNonnullByDefault
public class MemoryMembershipService extends AbstractService implements MembershipService {
    private static final Logger LOG = LoggerFactory.getLogger(MemoryMembershipService.class);

    private final Map<Group, Set<Group>> childGroupsByParent = new ConcurrentHashMap<>();
    private final Map<Group, Set<User>> usersByGroup = new ConcurrentHashMap<>();

    public MemoryMembershipService(Services services) {
        super(services);
    }

    @Override
    public synchronized void addGroupToGroup(Group child, Group parent) {
        requireExists(parent);
        requireExists(child);

        Set<Group> children = childGroupsByParent.get(parent);
        if (children == null) {
            children = ConcurrentHashMap.newKeySet();
            childGroupsByParent.put(parent, children);
        }
        children.add(child);

        LOG.debug("Added child group " + child + " to parent group " + parent);
    }

    public synchronized void addUserToGroup(User user, Group group) {
        requireExists(user);
        requireExists(group);

        Set<User> users = usersByGroup.get(group);
        if (users == null) {
            users = ConcurrentHashMap.newKeySet();
            usersByGroup.put(group, users);
        }
        users.add(user);

        LOG.debug("Added user " + user + " to group " + group);
    }

    public boolean isUserInGroup(User user, Group group) {
        requireNonNull(user, "user");
        requireNonNull(group, "group");

        // DONE! Only knows direct memberships right now
        Collection<User> usersInGroup = getUsersInGroup(group);
        if (usersInGroup.contains(user)) {
            return true;
        }
        Set<Group> childGroups = childGroupsByParent.getOrDefault(group, ConcurrentHashMap.newKeySet());
        for (Group childGroup : childGroups) {
            if (isUserInGroup(user, childGroup)) {
                return true;
            }
        }
        return false;
    }

    public boolean isGroupInGroup(Group child, Group parent) {
        requireNonNull(child, "child");
        requireNonNull(parent, "parent");

        // DONE... Only doing one level for now
        Collection<Group> children = childGroupsByParent.getOrDefault(parent, ConcurrentHashMap.newKeySet());

        if (children.contains(child)) {
            return true;
        }
        for (Group nextParent : children) {
            if (isGroupInGroup(child, nextParent)) {
                return true;
            }
        }

        return false;
    }

    public Collection<User> getUsersInGroup(Group group) {
        requireNonNull(group, "group");

        final Collection<User> users = usersByGroup.getOrDefault(group, ConcurrentHashMap.newKeySet());
        LOG.debug("Current users in group {}: {}", group.toString(), users.toString());
        return users;
    }

    @Override
    public void removeGroupFromGroup(Group child, Group parent) {
        requireNonNull(parent, "parent");
        requireNonNull(child, "child");

        Set<Group> children = childGroupsByParent.get(parent);
        if (children != null) {
            children.remove(child);
        }
    }

    public void removeUserFromGroup(User user, Group group) {
        requireNonNull(user, "user");
        requireNonNull(group, "group");

        getUsersInGroup(group).remove(user);
        LOG.debug(String.format("Removed user %s from group %s", user, group));
    }

    @Override
    public void removeAllUsersFromGroup(Group group) {

    }

    private void requireExists(User user) {
        requireNonNull(user, "user");
        if (services.getUserService().findByName(user.getName()) == null) {
            throw new IllegalArgumentException("User '" + user + "' does not exist!");
        }
    }

    private void requireExists(Group group) {
        requireNonNull(group, "group");
        if (services.getGroupService().findByName(group.getName()) == null) {
            throw new IllegalArgumentException("Group '" + group + "' does not exist!");
        }
    }
}
