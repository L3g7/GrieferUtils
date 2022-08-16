package dev.l3g7.griefer_utils.asm;

import dev.l3g7.griefer_utils.asm.annotations.ClassTarget;
import dev.l3g7.griefer_utils.asm.annotations.MethodTarget;
import dev.l3g7.griefer_utils.asm.mappings.MappingNode;
import dev.l3g7.griefer_utils.asm.mappings.Mappings;
import dev.l3g7.griefer_utils.asm.util.ASMUtil;
import dev.l3g7.griefer_utils.file_provider.FileProvider;
import dev.l3g7.griefer_utils.util.Reflection;
import dev.l3g7.griefer_utils.util.VersionUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Transformer implements IClassTransformer {

    private final Map<String, Class<?>> editors = new HashMap<>();

    public Transformer() {
        if (VersionUtil.isForge()) {
            FileProvider
                    .getClassesInPackage("dev.l3g7.griefer_utils.asm.editors")
                    .filter(clazz -> clazz.isAnnotationPresent(ClassTarget.class))
                    .forEach(clazz -> editors.put(clazz.getDeclaredAnnotation(ClassTarget.class).value(), clazz));
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (editors.containsKey(transformedName)) {
            Mappings.obfuscated = !name.equals(transformedName);
            try {
                // Read array to node
                ClassNode classNode = new ClassNode();
                ClassReader reader = new ClassReader(basicClass);
                reader.accept(classNode, 0);

                // Loop through edit methods
                for(Method editMethod : editors.get(transformedName).getDeclaredMethods()) {
                    if(!editMethod.isAnnotationPresent(MethodTarget.class))
                        continue;

                    // Resolve mapping
                    MappingNode.Method target = resolveMapping(transformedName, editMethod.getAnnotation(MethodTarget.class));

                    // Call method
                    for(MethodNode methodNode : classNode.methods) {
                        if(methodNode.name.equals(target.toString()) && methodNode.desc.equals(ASMUtil.getDescription(target))) {
                            ASMUtil.updateMethod(methodNode);
                            Reflection.invoke(editMethod, null);
                        }
                    }

                }

                // Write node to array
                ClassWriter writer = new ClassWriter(3);
                classNode.accept(writer);
                return writer.toByteArray();
            } catch (IllegalArgumentException | SecurityException e) {
                e.printStackTrace();
            }
        }
        return basicClass;
    }

    private static MappingNode.Method resolveMapping(String transformedClassName, MethodTarget target) {
        MappingNode.Class mClass = Mappings.getClass(transformedClassName);

        mtdLoop:
        for(MappingNode.Method m : mClass.getMethods()) {

            // Check if mapping applies
            if (!m.unobfuscated.equals(target.name()))
                continue;
            if(m.returnType != Mappings.getClass(target.returnValue()))
                continue;

            // Check parameters
            if (m.params.length != target.parameters().length)
                continue;
            for (int i = 0; i < m.params.length; i++) {
                if (m.params[i] != Mappings.getClass(target.parameters()[i]))
                    continue mtdLoop;
            }

            return m;
        }

        throw new RuntimeException("Could not find mappings for '" + target.name() + "' !");
    }
}
