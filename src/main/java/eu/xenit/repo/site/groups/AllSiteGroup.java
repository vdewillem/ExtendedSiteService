package eu.xenit.repo.site.groups;

import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * User: willem
 * Date: 6/19/14
 */
public class AllSiteGroup extends SiteGroup{
    private static final Log logger = LogFactory.getLog(AllSiteGroup.class);

    private String name, role;

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    private AuthorityService authorityService;

    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Override
    public boolean isRelevant(String shortName) {
        return true;
    }

    @Override
    public boolean isRelevant(String shortName, String roleFilter) {
        if(roleFilter == null)
            return true;
        else
            return roleFilter.equals(this.role);
    }

    @Override
    public boolean isMember(String authorityName) {
        return isMemberOfGroup(authorityName, this.name);
    }

    @Override
    public Set<String> getMembers() {
        try{
            return this.authorityService.getContainedAuthorities(AuthorityType.USER, this.name, false); //TODO handle non-existing name
        }
        catch(UnknownAuthorityException e){
            if(logger.isWarnEnabled())
                logger.warn(String.format("Couldn't fetch members of unknown '%s'", this.name));
            return new HashSet<String>();
        }
    }

    private boolean isMemberOfGroup(String user, String group)
    {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("isMemberOfGroup(%s,%s)", user, group));
        }

        if(group == null || group.equals(""))
            return false;

        if(!group.startsWith("GROUP_"))
            group = "GROUP_" + group;
        try {
            //TODO: maybe the other way around. Now +- 300 users in group vs 20 groups per user ?!
            Set<String> users = this.authorityService.getContainedAuthorities(AuthorityType.USER, group, false);
            return users.contains(user);
        } catch(UnknownAuthorityException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Unknown Authority: \"%s\"", group), e);
            }
            return false;
        }
    }

}