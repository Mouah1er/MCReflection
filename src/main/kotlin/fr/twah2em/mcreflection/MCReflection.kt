package fr.twah2em.mcreflection

import org.apache.commons.lang3.Validate
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

private val CRAFT_BUKKIT_PREFIX = Bukkit.getServer().javaClass.`package`.name
private val NMS_PREFIX = CRAFT_BUKKIT_PREFIX.replace("org.bukkit.craftbukkit", "net.minecraft.server")

val version = Integer.parseInt(Bukkit.getBukkitVersion());

fun sendPacket(receiver: Player, packet: Any) {
    Validate.notNull(receiver, "Receiver cannot be null")
    Validate.notNull(packet, "Packet cannot be null")

    val packetClass = NMSClass("Packet")
    val entityPlayerClass = NMSClass("EntityPlayer")
    val playerConnectionField = field(entityPlayerClass!!, "playerConnection")

    val sendPacketMethod = method(playerConnectionField!!.type, "sendPacket", packetClass!!)

    val entityPlayer = handle(receiver)
    val playerConnection = playerConnectionField.get(entityPlayer)

    sendPacketMethod.invoke(playerConnection, packet)
}

fun packet(className: String, vararg arguments: Any): Any {
    val packetClass = NMSClass(className)

    val classes = arguments.map { it.javaClass }.toTypedArray()
    val packetConstructor = constructor(packetClass!!, *classes)

    return invokeConstructor(packetConstructor!!, arguments)
}

fun handle(craftBukkitObj: Any): Any? {
    return try {
        invokeMethod<Any>(method(craftBukkitObj.javaClass, "getHandle"), craftBukkitObj)
    } catch (e: Exception) {
        null
    }
}

fun NMSClass(className: String): Class<*>? {
    try {
      if (version < 1.17) {
        return Class.forName("$NMS_PREFIX.$className")
      } else if (version >= 1.17) {
      	  return Class.forName("net.minecraft.$className")
      }
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }

    return null
}

fun craftBukkitClass(className: String): Class<*>? {
    try {
        return Class.forName("$CRAFT_BUKKIT_PREFIX.$className")
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
    }

    return null
}

fun field(clazz: Class<*>, fieldName: String): Field? {
    return clazz.getDeclaredField(fieldName)
}

fun field(field: Field, obj: Any, value: Any) {
    try {
        field.isAccessible = true
        field.set(obj, value)
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    }
}

fun method(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
    return clazz.getDeclaredMethod(methodName, *parameterTypes)
}

fun <T> invokeMethod(method: Method, obj: Any, vararg args: Any): T {
    method.isAccessible = true

    return method.invoke(obj, args) as T
}

fun <T : Enum<T>> enumValue(enumFullName: Class<T>, enumValue: String): T {
    return enumFullName.cast(
        enumFullName.javaClass.getDeclaredMethod("valueOf", String::class.java).invoke(enumFullName, enumValue)
    )
}

fun <T> constructor(clazz: Class<T>, vararg parameterTypes: Class<*>): Constructor<T>? {
    return clazz.getDeclaredConstructor(*parameterTypes)
}

fun <T> invokeConstructor(constructor: Constructor<T>, vararg parameterTypes: Any): T {
    constructor.isAccessible = true

    return constructor.newInstance(parameterTypes)
}