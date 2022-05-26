package com.viloveul.context.auth.dto;

import com.viloveul.context.type.SignerType;
import com.viloveul.context.auth.model.GroupModel;
import com.viloveul.context.auth.model.UserModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@EqualsAndHashCode
@NoArgsConstructor
@JsonSerialize
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailAuthentication implements UserDetails {

    @JsonIgnore
    private Date now = new Date();

    @JsonProperty(value = "id")
    private String id;

    @JsonProperty(value = "username")
    private String username;

    @JsonIgnore
    private String password;

    @JsonProperty(value = "email")
    private String email;

    @JsonProperty(value = "type")
    private SignerType type;

    @JsonProperty(value = "fullname")
    private String fullname;

    @JsonProperty(value = "status")
    private Boolean status;

    @JsonProperty(value = "start_date")
    private Date startDate;

    @JsonProperty(value = "end_date")
    private Date endDate;

    @JsonProperty(value = "session")
    private String session;

    @JsonProperty(value = "group")
    private Group group;

    @JsonProperty(value = "accessors")
    private Collection<String> accessors = new ArrayList<>();

    @JsonProperty(value = "abilities")
    private Collection<GroupMapper> abilities = new ArrayList<>();

    @JsonProperty(value = "extras")
    private Map<String, Serializable> extras = new HashMap<>();

    @JsonProperty(value = "relations")
    private Map<String, List<Map<String, Serializable>>> relations = new HashMap<>();

    @JsonIgnore
    private transient Collection<GrantedAuthority> authorities;

    @JsonIgnore
    private boolean initialized = false;

    public DetailAuthentication(
        UserModel user,
        Map<String, Serializable> extras,
        Map<String, List<Map<String, Serializable>>> relations
    ) {
        this(user);
        this.relations.putAll(relations);
        this.extras.putAll(extras);
    }

    public DetailAuthentication(SignerType signer, String context) {
        Calendar calendar = Calendar.getInstance();
        this.startDate = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        this.endDate = calendar.getTime();
        this.id = UUID.randomUUID().toString();
        this.username = context;
        this.type = signer;
        this.status = true;
    }

    public DetailAuthentication(UserModel user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.fullname = user.getFullname();
        this.type = user.getType();
        this.status = user.getStatus();
        this.startDate = user.getStartDate();
        this.endDate = user.getEndDate();
        this.group = new Group(user.getGroup());
    }

    @JsonIgnore
    public Collection<Object> getPrivileges(Predicate<GroupMapper> callback) {
        Collection<Object> includes = new ArrayList<>();
        Collection<DetailAuthentication.GroupMapper> groups = this.getAbilities();
        for (DetailAuthentication.GroupMapper g : groups) {
            if (callback == null || callback.test(g)) {
                for (String pri : g.getAuthorities()) {
                    if (pri.endsWith("_PRIVILEGE")) {
                        String[] parts = pri.split("_");
                        includes.add(String.join("_", Arrays.copyOfRange(parts, 1, parts.length - 1)));
                    }
                }
            }
        }
        return includes;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return this.endDate == null || this.endDate.before(this.now);
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(this.status);
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.startDate == null || this.startDate.before(this.now);
    }

    public void addAbility(GroupMapper group) {
        if (!this.abilities.contains(group)) {
            this.abilities.add(group);
        }
    }

    public <T> T getExtra(String key, Serializable def, Class<T> tClass) {
        return tClass.cast(this.extras.getOrDefault(key, def));
    }

    public Object getExtra(String key, Serializable def) {
        return this.extras.getOrDefault(key, def);
    }

    public Object getExtra(String key) {
        return this.extras.get(key);
    }

    public List<Map<String, Serializable>> getRelations(String key) {
        return this.relations.getOrDefault(key, new ArrayList<>());
    }

    public void initialize() {
        if (!this.isInitialized()) {
            Collection<String> tmps = this.accessors;
            for (GroupMapper map : this.abilities) {
                for (String ability : map.getAuthorities()) {
                    if (!tmps.contains(ability)) {
                        tmps.add(ability);
                    }
                }
            }
            this.authorities = tmps.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Group implements Serializable {

        private String id;

        private String name;

        private Group parent;

        public Group(GroupModel model) {
            this.id = model.getId();
            this.name = model.getName();
            if (model.getParent() != null) {
                this.parent = new Group(model.getParent());
            }
        }

    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GroupMapper implements Serializable {

        private Collection<String> authorities = new ArrayList<>();

        private Collection<String> groups = new ArrayList<>();

        public GroupMapper(GroupModel model) {
            this.groups.add(model.getId());
            this.addSubordinate(model.getChilds());
        }

        public void addAuthority(Collection<String> authorities) {
            this.authorities.removeAll(authorities);
            this.authorities.addAll(authorities);
        }

        private void addSubordinate(Iterable<? extends GroupModel> groups) {
            for (GroupModel child : groups) {
                if (!this.groups.contains(child.getId())) {
                    this.groups.add(child.getId());
                    this.addSubordinate(child.getChilds());
                }
            }
        }
    }
}
