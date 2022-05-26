package com.viloveul.context.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PreUpdate;
import java.util.Date;

@Setter
@Getter
@MappedSuperclass
public abstract class AbstractFullEntity extends AbstractMidEntity {

    @JsonIgnore
    @Column(name = "deleted_at", insertable = false)
    protected Date deletedAt;

    @JsonIgnore
    @Column(name = "deleted_by", insertable = false)
    protected String deletedBy;

    @PreUpdate
    @Override
    public void onBeforeUpdate() {
        if (this.deletedAt == null) {
            super.onBeforeUpdate();
        } else {
            this.deletedBy = this.getIncludeAuthoredBy();
        }
    }

}
