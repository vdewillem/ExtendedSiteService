package eu.xenit.repo.site.groups;

import eu.xenit.repo.site.ExtendedSiteServiceImpl;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.alfresco.repo.security.permissions.impl.AccessPermissionImpl;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;

import java.util.HashSet;
import java.util.Set;

/**
 * User: willem
 * Date: 7/17/14
 */
public class SiteGroupTest extends TestCase {
    private SiteGroup adminGroup;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AllSiteGroup siteGroup = new AllSiteGroup();
        siteGroup.setName("GROUP_ALFRESCOSITES_ADMIN");
        siteGroup.setRole("SiteManager");
        this.adminGroup = siteGroup;
    }

    public void testGetActualRole()
    {
        Set<AccessPermission> permissions = new HashSet<>();
        permissions.add(new AccessPermissionImpl("SiteConsumer", AccessStatus.ALLOWED, "GROUP_site_20400_SiteManager", 0));
        permissions.add(new AccessPermissionImpl("SiteContributor", AccessStatus.ALLOWED, "GROUP_site_20400_SiteContributor", 1));

        Assert.assertEquals("SiteConsumer", adminGroup.getActualRole(permissions, "20400"));
        Assert.assertEquals(null, adminGroup.getActualRole(permissions, "20000"));
    }

    public void testMigrate()
    {
        AccessPermission accessPermission = ExtendedSiteServiceImpl.expand(
                new AccessPermissionImpl("SiteConsumer", AccessStatus.ALLOWED, "GROUP_site_20400_SiteManager", 0),
                "20400",
                this.adminGroup
        );
        Assert.assertEquals("SiteConsumer",accessPermission.getPermission());
        Assert.assertEquals("GROUP_ALFRESCOSITES_ADMIN",accessPermission.getAuthority());
    }
}