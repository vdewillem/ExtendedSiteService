package eu.xenit.repo.site.groups;

import org.alfresco.service.cmr.security.AccessPermission;

import java.util.Set;

/**
 * User: willem
 * Date: 6/19/14
 */
public abstract class SiteGroup {
    public abstract boolean isRelevant(String shortName);
    public abstract boolean isRelevant(String shortName, String roleFilter);
    public abstract boolean isMember(String authorityName);
    public abstract Set<String> getMembers();
    public abstract String getName();
    public abstract String getRole();

    public String getActualRole(Set<AccessPermission> permissions, String site)
    {
        for(AccessPermission p : permissions)
        {
            String role = this.getActualRole(p,site);
            if(role != null)
                return role;
        }
        return null;
    }

    public String getActualRole(AccessPermission p, String site)
    {
        if(this.isRelevant(site) && p.getAuthority().equals(String.format("GROUP_site_%s_%s",site,this.getRole())))
            return p.getPermission();
        return null;
    }
}