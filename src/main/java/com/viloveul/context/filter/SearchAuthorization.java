package com.viloveul.context.filter;

import com.viloveul.context.auth.AccessControlCollection;
import com.viloveul.context.auth.model.OwnerModel;
import com.viloveul.context.auth.model.PermissionModel;
import com.viloveul.context.type.SignerType;
import com.viloveul.context.auth.dto.DetailAuthentication;
import com.viloveul.context.util.helper.FieldHelper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class SearchAuthorization<T> implements Specification<T> {

    private static final List<SignerType> UTYPES = Arrays.asList(SignerType.ADMIN, SignerType.SYSTEM);

    protected String resource;

    protected String operation;

    protected String user;

    protected String group;

    protected boolean authorized;

    protected boolean access;

    protected DetailAuthentication authentication;

    protected transient List<Allow> allows = new ArrayList<>();

    protected transient Collection<String> groups = new ArrayList<>();

    protected Configuration configuration;

    public SearchAuthorization(
        Authentication authentication,
        @NonNull Configuration configuration,
        @NonNull String resource,
        @NonNull String operation,
        String field
    ) {
        this(authentication, configuration, resource, operation, new UserAllow(field));
    }

    public SearchAuthorization(
        Authentication authentication,
        @NonNull Configuration configuration,
        @NonNull String resource,
        @NonNull String operation,
        Allow... allows
    ) {
        this(authentication, configuration, resource, operation);
        if (allows != null) {
            this.allows.addAll(Arrays.asList(allows));
        }
    }

    public SearchAuthorization(
        Authentication authentication,
        @NonNull Configuration configuration,
        @NonNull SearchProperties properties
    ) {
        this(authentication, configuration, properties.resource(), properties.operation());
        for (SearchProperties.Allow allow : properties.allows()) {
            if (allow.option() == SearchProperties.Option.USER) {
                this.allows.add(new UserAllow(allow.field()));
            } else {
                this.allows.add(new GroupAllow(allow.field()));
            }
        }
        if (properties.customizer()) {
            this.allows.add((search, root, builder, query) -> {
                if (AccessControlCollection.hasConfiguration(search.resource)) {
                    @SuppressWarnings("unchecked")
                    Root<T> rootCast = (Root<T>) root;
                    Specification<T> specification = AccessControlCollection.fetchSpecification(search.resource, search.operation, search.authentication);
                    return specification.toPredicate(rootCast, query, builder);
                }
                return builder.disjunction();
            });
        }
    }

    public SearchAuthorization(
        Authentication authentication,
        @NonNull Configuration configuration,
        @NonNull String resource,
        @NonNull String operation
    ) {
        this.authentication = (DetailAuthentication) authentication.getDetails();
        String authority = operation.concat("-").concat(resource);
        this.user = this.authentication.getId();
        this.group = this.authentication.getGroup().getId();
        this.authorized = UTYPES.contains(this.authentication.getType());
        this.resource = resource;
        this.operation = operation;
        this.access = this.authentication.getAccessors().contains(authority);
        this.configuration = configuration;
        for (DetailAuthentication.GroupMapper mapper : this.authentication.getAbilities()) {
            if (mapper.getAuthorities().contains(authority)) {
                this.groups.addAll(mapper.getGroups());
            }
        }
    }

    @Override
    public Predicate toPredicate(
        @NonNull Root<T> root,
        @NonNull CriteriaQuery<?> query,
        @NonNull CriteriaBuilder builder
    ) {
        Predicate adminPredicate = this.authorized ? builder.conjunction() : builder.disjunction();
        Predicate userPredicate = this.buildWithAllowedUser(root, builder, query);
        Predicate ownerPredicate = this.buildWithAccessOwner(root, builder, query);
        Predicate permissionPredicate = this.buildWithAccessPermission(root, builder, query);
        return builder.and(builder.or(adminPredicate, userPredicate, ownerPredicate, permissionPredicate));
    }

    protected Predicate buildWithAllowedUser(
        @NonNull Root<T> root,
        @NonNull CriteriaBuilder builder,
        @NonNull CriteriaQuery<?> query
    ) {
        Collection<Predicate> predicates = new ArrayList<>();
        if (!this.allows.isEmpty()) {
            for (Allow allow : this.allows) {
                predicates.add(allow.getPredicate(this, root, builder, query));
            }
        }
        return predicates.isEmpty() ? builder.disjunction() : (builder.or(predicates.toArray(new Predicate[0])));
    }

    protected Predicate buildWithAccessOwner(
        @NonNull Root<T> root,
        @NonNull CriteriaBuilder builder,
        @NonNull CriteriaQuery<?> query
    ) {
        Subquery<? extends OwnerModel> queryOwner = query.subquery(this.configuration.owner);
        Root<? extends OwnerModel> rootOwner = queryOwner.from(this.configuration.owner);
        Collection<Predicate> predicates = new ArrayList<>();
        Predicate userOwner = builder.and(
            builder.equal(rootOwner.get("group"), this.group),
            builder.equal(rootOwner.get("user"), this.user),
            this.access ? builder.conjunction() : builder.disjunction()
        );
        Predicate groupOwner = this.groups.isEmpty() ? builder.disjunction() : rootOwner.get("group").in(this.groups);
        predicates.add(builder.or(userOwner, groupOwner));
        predicates.add(builder.equal(rootOwner.get("resource"), this.resource));
        predicates.add(builder.equal(root.get("id"), rootOwner.get("object")));
        queryOwner.select(rootOwner.get("id"));
        queryOwner.where(predicates.toArray(new Predicate[0]));
        return builder.exists(queryOwner);
    }

    protected Predicate buildWithAccessPermission(
        @NonNull Root<T> root,
        @NonNull CriteriaBuilder builder,
        @NonNull CriteriaQuery<?> query
    ) {
        Subquery<? extends PermissionModel> queryPermission = query.subquery(this.configuration.permission);
        Root<? extends PermissionModel> rootPermission = queryPermission.from(this.configuration.permission);
        queryPermission.select(rootPermission.get("id"));
        queryPermission.where(
            builder.and(
                builder.equal(rootPermission.get("resource"), this.resource),
                builder.equal(rootPermission.get("operation"), this.operation),
                builder.equal(rootPermission.get("user"), this.user),
                builder.equal(root.get("id"), rootPermission.get("object")),
                builder.equal(rootPermission.get("status"), true)
            )
        );
        return builder.exists(queryPermission);
    }

    public interface Allow {
        Predicate getPredicate(
            SearchAuthorization<?> search,
            @NonNull Root<?> root,
            @NonNull CriteriaBuilder builder,
            @NonNull CriteriaQuery<?> query
        );
    }

    public static class GroupAllow implements Allow {

        private final String value;

        public GroupAllow(String value) {
            this.value = value;
        }

        public Predicate getPredicate(
            SearchAuthorization<?> search,
            @NonNull Root<?> root,
            @NonNull CriteriaBuilder builder,
            @NonNull CriteriaQuery<?> query
        ) {
            return builder.and(
                ! search.groups.isEmpty()
                ? FieldHelper.fillFieldSelector(root, this.value).in(search.groups)
                : builder.disjunction()
            );
        }
    }

    public static class UserAllow implements Allow {

        private final String value;

        public UserAllow(String value) {
            this.value = value;
        }

        public Predicate getPredicate(
            SearchAuthorization<?> search,
            @NonNull Root<?> root,
            @NonNull CriteriaBuilder builder,
            @NonNull CriteriaQuery<?> query
        ) {
            return builder.and(
                ! search.access
                ? builder.disjunction()
                : builder.equal(FieldHelper.fillFieldSelector(root, this.value), search.user)
            );
        }
    }

    public static class Configuration implements Serializable {

        protected Class<? extends OwnerModel> owner;

        protected Class<? extends PermissionModel> permission;

        public Configuration(Class<? extends OwnerModel> owner, Class<? extends PermissionModel> permission) {
            this.owner = owner;
            this.permission = permission;
        }
    }
}
