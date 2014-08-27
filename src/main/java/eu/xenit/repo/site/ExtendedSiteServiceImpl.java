package eu.xenit.repo.site;

import eu.xenit.repo.site.groups.SiteGroup;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.repo.site.SiteMemberInfoImpl;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteMemberInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Arrays.asList;

public class ExtendedSiteServiceImpl extends SiteServiceImpl
{
    private final static Logger logger = LoggerFactory.getLogger(ExtendedSiteServiceImpl.class);

    private final static List<String> siteRoles = asList("SiteConsumer", "SiteCollaborator", "SiteManager");

    private List<SiteGroup> siteGroups = new ArrayList<SiteGroup>();

    private PermissionService permissionService;

    public void setSiteGroups(List<SiteGroup> siteGroups) {
        this.siteGroups.addAll(siteGroups);
    }

    public List<SiteGroup> getSiteGroups() {
        return siteGroups;
    }

    public void setPermissionService2(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public boolean isMember(String shortName, String authorityName) {
        if(logger.isDebugEnabled())
            logger.debug(String.format("isMember('%s','%s')",shortName,authorityName));

        if(super.isMember(shortName, authorityName))
            return true;

        for(SiteGroup siteGroup : this.siteGroups){
            if(siteGroup.isRelevant(shortName, null) && siteGroup.isMember(authorityName)){
                return true;
            }
        }

        return false;
    }

    @Override
    public Map<String, String> listMembers(String shortName, String nameFilter, String roleFilter, int size, boolean collapseGroups) {
        if(logger.isDebugEnabled())
            logger.debug(String.format("listMembers(%s,%s)",shortName,roleFilter));
        Map<String, String> members =  super.listMembers(shortName, nameFilter, roleFilter, size, collapseGroups);

        for(SiteGroup siteGroup : this.siteGroups){
            if(siteGroup.isRelevant(shortName, roleFilter)){
                for(String user : siteGroup.getMembers())
                    members.put(user,roleFilter);
            }
        }

        return members;
    }

    @Override
    public SiteMemberInfo getMembersRoleInfo(String shortName, String authorityName) {
        if (!authorityName.startsWith("GROUP_") && !authorityName.startsWith("AUTHORITY_")) {
            // return the highest ranking membership role we can find
            SiteGroup bestGroup = null;
            for(SiteGroup siteGroup : this.siteGroups){
                if(siteGroup.isRelevant(shortName, null) && siteGroup.isMember(authorityName)){
                    final String role = siteGroup.getRole();
                    if ("SiteManager".equals(role)) {
                        // can't get better than this
                        return new SiteMemberInfoImpl(authorityName, role, true);
                    }
                    bestGroup = bestOf(bestGroup, siteGroup);
                }
            }

            if (bestGroup != null) {
                return new SiteMemberInfoImpl(authorityName, bestGroup.getRole(), true);
            }
        }

        return super.getMembersRoleInfo(shortName, authorityName);
    }

    private SiteGroup bestOf(SiteGroup a, SiteGroup b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }

        return Integer.compare(siteRoles.indexOf(a.getRole()), siteRoles.indexOf(b.getRole())) == 1 ? a : b;
    }

    public void expand(NodeRef nodeRef)
    {
        SiteInfo siteInfo = this.getSite(nodeRef);
        if(siteInfo == null){
            logger.warn(String.format("'%s' is not a site", nodeRef));
            return;
        }

        Set<AccessPermission> permissions = this.permissionService.getAllSetPermissions(nodeRef);
        for(AccessPermission permission : permissions)
        {
            for(SiteGroup siteGroup : this.getSiteGroups()){
                AccessPermission newPermission = expand(permission, siteInfo.getShortName(), siteGroup);
                if(newPermission != null)
                    this.permissionService.setPermission(nodeRef, newPermission.getAuthority(), newPermission.getPermission(), true);
            }
        }
    }

    public static AccessPermission expand(AccessPermission p, String site, SiteGroup siteGroup) {
        String role = siteGroup.getActualRole(p,site);
        if(role == null)
            return null;

        return new AccessPermissionImpl(p.getPermission(), p.getAccessStatus(), siteGroup.getName(),0);
    }
}