/*
 * This file is part of GrieferUtils (https://github.com/L3g7/GrieferUtils).
 * Copyright (c) L3g7.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package dev.l3g7.griefer_utils.core.api.misc;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import dev.l3g7.griefer_utils.core.api.mapping.Mapper;
import dev.l3g7.griefer_utils.core.api.reflection.Reflection;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import sun.misc.Unsafe;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static dev.l3g7.griefer_utils.core.api.mapping.Mapping.UNOBFUSCATED;

public class UnsafeJsonSerializer {

	public static final Unsafe UNSAFE;

	private static final Map<Class<?>, PrimitiveSerializer> PRIMITIVE_SERIALIZERS = ImmutableMap.<Class<?>, PrimitiveSerializer>builder()
		.put(byte.class,    new PrimitiveSerializer(Unsafe::getByte,    Array::getByte))
		.put(short.class,   new PrimitiveSerializer(Unsafe::getShort,   Array::getShort))
		.put(int.class,     new PrimitiveSerializer(Unsafe::getInt,     Array::getInt))
		.put(long.class,    new PrimitiveSerializer(Unsafe::getLong,    Array::getLong))
		.put(float.class,   new PrimitiveSerializer(Unsafe::getFloat,   Array::getFloat))
		.put(double.class,  new PrimitiveSerializer(Unsafe::getDouble,  Array::getDouble))
		.put(char.class,    new PrimitiveSerializer(Unsafe::getChar,    Array::getChar))
		.put(boolean.class, new PrimitiveSerializer(Unsafe::getBoolean, Array::getBoolean))
		.build();

	private final List<Object> currentPath = new ArrayList<>();

	public static JsonElement toJson(Object o) {
		return new UnsafeJsonSerializer().toJson0(o);
	}

	public JsonElement toJson0(Object o) {
		return switch (o) {
			case null -> JsonNull.INSTANCE;
			case Number n -> new JsonPrimitive(n);
			case Character c -> new JsonPrimitive(c);
			case Boolean b -> new JsonPrimitive(b);
			case String s -> new JsonPrimitive(s);
			case IChatComponent icc -> new JsonPrimitive(IChatComponent.Serializer.componentToJson(icc));
			case Block block -> new JsonPrimitive(Block.blockRegistry.getNameForObject(block).toString());
			case ItemStack stack -> new JsonPrimitive(stack.writeToNBT(new NBTTagCompound()).toString());
			case IBlockState state -> {
				JsonObject object = new JsonObject();
				object.add("block", toJson0(state.getBlock()));
				for (Map.Entry<IProperty, Comparable> entry : state.getProperties().entrySet())
					object.addProperty(entry.getKey().getName(), entry.getValue().toString());
				yield object;
			}
			default -> {
				for (int i = 0; i < currentPath.size(); i++) {
					Object obj = currentPath.get(i);
					if (obj != o)
						continue;

					int dotdots = currentPath.size() - i;
					StringBuilder sb = new StringBuilder(dotdots * 3);
					for (int j = 0; j < dotdots; j++)
						sb.append("../");

					yield new JsonPrimitive(sb.toString());
				}

				currentPath.add(o);

				if (o.getClass().isArray())
					o = arrayToList(o);

				yield switch (o) {
					case Iterable<?> it -> {
						JsonArray array = new JsonArray();
						for (Object itO : it)
							array.add(toJson0(itO));

						yield array;
					}
					case Map<?, ?> m -> toJson0(m.entrySet());
					case Enum<?> e -> new JsonPrimitive(e.ordinal() + " / " + e.name());
					default -> serializeUnsafe(o);
				};
			}
		};
	}

	private JsonElement serializeUnsafe(Object o) {

		Class<?> clazz = o.getClass();
		JsonObject result = new JsonObject();

		while (clazz != Object.class) {
			for (Field declaredField : clazz.getDeclaredFields()) {
				if ((declaredField.getModifiers() & Modifier.STATIC) != 0)
					continue;

				String name = declaredField.getName();
				if (Mapper.isObfuscated())
					name = Mapper.mapField(clazz, name, Reflection.getMappingTarget(), UNOBFUSCATED);

				Object value = get(o, UNSAFE.objectFieldOffset(declaredField), declaredField.getType());
				result.add(name, toJson0(value));
			}
			clazz = clazz.getSuperclass();
		}

		currentPath.remove(o);
		return result;
	}

	private static Object get(Object o, long offset, Class<?> type) {
		if (!type.isPrimitive())
			return UNSAFE.getObject(o, offset);

		return PRIMITIVE_SERIALIZERS.get(type).unsafeGetter.get(UNSAFE, o, offset);
	}

	private static List<Object> arrayToList(Object o) {
		Class<?> component = o.getClass().getComponentType();
		if (!component.isPrimitive())
			return Arrays.asList((Object[]) o);

		BiFunction<Object, Integer, Object> arrayGetter = PRIMITIVE_SERIALIZERS.get(component).arrayGetter;
		int length = Array.getLength(o);
		List<Object> list = new ArrayList<>(Array.getLength(o));

		for (int i = 0; i < length; i++)
			list.add(arrayGetter.apply(o, i));

		return list;
	}

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe) unsafeField.get(null);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

	}

	private record PrimitiveSerializer(UnsafeGetter unsafeGetter, BiFunction<Object, Integer, Object> arrayGetter) {}

	@FunctionalInterface
	private interface UnsafeGetter {
		Object get(Unsafe unsafe, Object object, Long offset);
	}

}