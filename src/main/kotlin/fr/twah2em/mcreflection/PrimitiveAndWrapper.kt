package fr.twah2em.mcreflection

enum class PrimitiveAndWrapper(val primitive: Class<*>, val wrapper: Class<*>) {
    BYTE(Byte::class.java, Class.forName("java.lang.Byte")),
    SHORT(Short::class.java, Class.forName("java.lang.Short")),
    INTEGER(Int::class.java, Class.forName("java.lang.Integer")),
    LONG(Long::class.java, Class.forName("java.lang.Long")),
    FLOAT(Float::class.java, Class.forName("java.lang.Float")),
    DOUBLE(Double::class.java, Class.forName("java.lang.Double")),
    BOOLEAN(Boolean::class.java, Class.forName("java.lang.Boolean")),

    ;

    companion object {
        private val CLASS_MAP: MutableMap<Class<*>, PrimitiveAndWrapper> = HashMap()

        init {
            for (dataType in values()) {
                CLASS_MAP[dataType.primitive] = dataType
                CLASS_MAP[dataType.wrapper] = dataType
            }
        }

        fun fromClass(clazz: Class<*>): PrimitiveAndWrapper? {
            return CLASS_MAP[clazz]
        }

        fun primitive(clazz: Class<*>): Class<*> {
            val type = fromClass(clazz)
            return type?.primitive ?: clazz
        }

        fun primitive(classes: Array<Class<*>>): Array<Class<*>> {
            val length = classes.size
            val types: Array<Class<*>> = Array(length) {
                primitive(classes[it])
            }

            return types
        }

        fun wrapper(clazz: Class<*>): Class<*> {
            val type = fromClass(clazz)
            return type?.wrapper ?: clazz
        }

        fun wrapper(classes: Array<Class<*>>): Array<Class<*>> {
            val length = classes.size
            val types: Array<Class<*>> = Array(length) {
                wrapper(classes[it])
            }

            return types
        }

        fun compare(primary: Array<Class<*>>?, secondary: Array<Class<*>>?): Boolean {
            if (primary == null || secondary == null || primary.size != secondary.size) {
                return false
            }
            for (index in primary.indices) {
                val primaryClass = primary[index]
                val secondaryClass = secondary[index]
                if (primaryClass == secondaryClass || primaryClass.isAssignableFrom(secondaryClass)) {
                    continue
                }
                return false
            }
            return true
        }
    }
}
