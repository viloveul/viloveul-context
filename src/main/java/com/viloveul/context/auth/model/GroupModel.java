package com.viloveul.context.auth.model;

import java.util.Set;

public interface GroupModel {

    String getId();

    String getName();

    GroupModel getParent();

    Set<? extends GroupModel> getChilds();

}
