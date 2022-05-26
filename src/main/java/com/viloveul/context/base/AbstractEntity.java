package com.viloveul.context.base;

import com.viloveul.context.ApplicationContainer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import java.util.Date;
import java.util.Optional;

@MappedSuperclass
public abstract class AbstractEntity implements AbstractEntitySerializable {

    private static final String ANONYMOUS = "anonymousUser";

    @Transient
    @JsonIgnore
    private String includeAuthoredBy;

    @Id
    @Setter
    @Getter
    @Column(name = "id", nullable = false)
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "UUID")
    protected String id;

    @Setter
    @Getter
    @Column(name = "created_at", updatable = false, insertable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createdAt;

    @Setter
    @Getter
    @Column(name = "created_by", updatable = false)
    protected String createdBy;

    @Setter
    @Getter
    @Column(name = "status")
    protected Boolean status;

    @Setter
    @Getter
    @Column(name = "vorder", insertable = false, updatable = false)
    protected Long order;

    @PrePersist
    public void onBeforeCreate() {
        if (this.status == null) {
            this.status = Boolean.TRUE;
        }
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
        this.createdBy = this.getIncludeAuthoredBy();
    }

    public void setIncludeAuthoredBy(String author) {
        if (author != null && !author.equalsIgnoreCase(ANONYMOUS)) {
            this.includeAuthoredBy = author;
        }
    }


    protected String getIncludeAuthoredBy() {
        String usernamePrincipal = this.includeAuthoredBy;
        if (usernamePrincipal == null) {
            usernamePrincipal = Optional.ofNullable(ApplicationContainer.getUsernameAuthentication()).orElse(ANONYMOUS);
        }

        String app = "application:";
        String user = "user:";

        if (!usernamePrincipal.startsWith(app) && !usernamePrincipal.startsWith(user)) {
            usernamePrincipal = ANONYMOUS.equalsIgnoreCase(usernamePrincipal) ?
                app.concat("anonymous") :
                    user.concat(usernamePrincipal);
        }

        return usernamePrincipal;
    }

}
