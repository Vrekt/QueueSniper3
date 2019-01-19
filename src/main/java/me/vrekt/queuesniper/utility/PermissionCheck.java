package me.vrekt.queuesniper.utility;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

/**
 * Check if we have all the permissions needed.
 */
public class PermissionCheck {

    private static final Permission[] PERMISSIONS = {Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION,
            Permission.MESSAGE_EMBED_LINKS, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.MESSAGE_READ,
            Permission.MESSAGE_MANAGE, Permission.MANAGE_CHANNEL, Permission.MESSAGE_MENTION_EVERYONE
    };

    public static boolean hasPermission(Member self) {
        return self.hasPermission(PERMISSIONS);
    }

    public static String getPermissionsRequired() {
        StringBuilder permissions = new StringBuilder();

        for (Permission p : PERMISSIONS) {
            permissions.append(p.getName());
            permissions.append("\n");
        }

        return permissions.toString();
    }

}
