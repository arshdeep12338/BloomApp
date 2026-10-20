// Workaround for AGP AndroidLocationsException when multiple Android location env vars (e.g. ANDROID_PREFS_ROOT and ANDROID_USER_HOME) are defined in the OS environment.
try {
    val pe = Class.forName("java.lang.ProcessEnvironment")
    for (field in pe.declaredFields) {
        if (java.lang.reflect.Modifier.isStatic(field.modifiers)) {
            field.isAccessible = true
            val obj = field.get(null)
            if (obj is MutableMap<*, *>) {
                try {
                    val keysToRemove = obj.keys.filter {
                        val keyStr = it.toString()
                        keyStr.equals("ANDROID_PREFS_ROOT", ignoreCase = true) ||
                                keyStr.equals("ANDROID_SDK_HOME", ignoreCase = true)
                    }
                    for (k in keysToRemove) {
                        obj.remove(k)
                    }
                } catch (_: Exception) {
                    // Ignore unmodifiable maps
                }
            }
        }
    }
} catch (_: Exception) {}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Bloom"
include(":app")
