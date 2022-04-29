package fr.twah2em.mcreflection

import org.apache.commons.lang3.Validate
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

private val CRAFT_BUKKIT_PREFIX = Bukkit.getServer().javaClass.packageName
private val NMS_PREFIX = CRAFT_BUKKIT_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server")

val version = CRAFT_BUKKIT_PREFIX.substring(CRAFT_BUKKIT_PREFIX.lastIndexOf(".") + 1) + "."

fun sendPacket(receiver: Player, packet: Any) {
    Validate.notNull(receiver, "Receiver cannot be null")
    Validate.notNull(packet, "Packet cannot be null")

    val packetClass = getNMSClass("Packet")
    val entityPlayerClass = getNMSClass("EntityPlayer")
    val playerConnectionField = getField(entityPlayerClass, "playerConnection")

    val sendPacketMethod = getMethod(playerConnectionField!!.type, "sendPacket", packetClass)

    val entityPlayer = getHandle(receiver)
    val playerConnection = playerConnectionField.get(entityPlayer)

    sendPacketMethod!!.invoke(playerConnection, packet)
}

fun getHandle(any: Any): Any? {
    Validate.notNull(any, "Object cannot be null")
    Validate.isInstanceOf(getCraftBukkitClass("CraftPlayer"), any, "Object must be CraftPlayer")

    try {
        return getMethod(any.javaClass, "getHandle")!!.invoke(any)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

fun getNMSClass(className: String): Class<*>? {
    Validate.notNull(className, "Class name cannot be null")

    try {
        return Class.forName("$NMS_PREFIX.$className")
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }

    return null
}

fun getCraftBukkitClass(className: String): Class<*>? {
    Validate.notNull(className, "Class name cannot be null")

    try {
        return Class.forName("$CRAFT_BUKKIT_PREFIX.$className")
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }

    return null
}

fun getField(clazz: Class<*>?, fieldName: String): Field? {
    Validate.notNull(clazz, "Class cannot be null")
    Validate.notNull(fieldName, "Field name cannot be null")

    try {
        return clazz!!.getDeclaredField(fieldName)
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    }

    return null
}

fun setField(field: Field, obj: Any, value: Any) {
    Validate.notNull(field, "Field cannot be null")
    Validate.notNull(obj, "Object cannot be null")

    try {
        field.isAccessible = true
        field.set(obj, value)
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    }
}

fun getMethod(clazz: Class<*>?, methodName: String, vararg parameterTypes: Class<*>?): Method? {
    Validate.notNull(clazz, "Class cannot be null")
    Validate.notNull(methodName, "Method name cannot be null")
    Validate.notNull(parameterTypes, "Parameter types cannot be null")

    try {
        return clazz!!.getDeclaredMethod(methodName, *parameterTypes)
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }

    return null
}

fun <T : Enum<T>> getEnumValue(enumFullName: Class<T>, enumValue: String): T {
    return enumFullName.cast(
        enumFullName.javaClass.getDeclaredMethod("valueOf", String::class.java).invoke(enumFullName, enumValue)
    )
}

fun getConstructor(clazz: Class<*>?, vararg parameterTypes: Class<*>?): Constructor<*>? {
    Validate.notNull(clazz, "Class cannot be null")
    Validate.notNull(parameterTypes, "Parameter types cannot be null")

    val primitiveTypes: Array<Class<*>> = Array(parameterTypes.size) {
        PrimitiveAndWrapper.primitive(parameterTypes[it]!!)
    }
    for (constructor in clazz!!.constructors) {
        if (!PrimitiveAndWrapper.compare(
                PrimitiveAndWrapper.primitive(
                    constructor.parameterTypes
                ), primitiveTypes
            )
        ) {
            continue
        }

        return constructor
    }

    return null
}