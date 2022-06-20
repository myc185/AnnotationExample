package com.lucky.annotation_compiler;


import com.google.auto.service.AutoService;
import com.lucky.annotation.BindView;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class BindViewProcessor extends AbstractProcessor {

    private static final String TAG = BindViewProcessor.class.getSimpleName();

    //1.支持的版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    //2.能用来处理哪些注解
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    //3.定义一个用来生成APT目录下面的文件的对象
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        //filter 用于后续写文件
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //打印测试
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "===>" + set);
        //获取APP中所有用到了BindView注解的对象
        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(BindView.class);

        // TypeElement -->类
        // ExecutableElement --> 方法
        // VariableElement--> 属性
        //开始对elementsAnnotatedWith进行分类：我们可能很多activity中都定义有 BindView注解，用activity作为key，List作为注解列表
        Map<String, List<VariableElement>> map = new HashMap<>();
        for (Element element : elementsAnnotatedWith) {
            VariableElement variableElement = (VariableElement) element;
            //例如MainActivity
            String activityName = variableElement.getEnclosingElement().getSimpleName().toString();
            Class aClass = variableElement.getEnclosingElement().getClass();
            List<VariableElement> variableElements = map.get(activityName);
            if (variableElements == null) {
                variableElements = new ArrayList<>();
                map.put(activityName, variableElements); //Activity跟注解列表是一对多的关系
            }
            variableElements.add(variableElement);
        }

        //开始遍历map, 生成每个activity对应的模板代码
        if (map.size() > 0) {
            Writer writer = null;
            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                String activityName = iterator.next();
                //对应Activity下面的注解列表
                List<VariableElement> variableElements = map.get(activityName);
                //获取Activity所在包名
                TypeElement enclosingElement = (TypeElement) variableElements.get(0).getEnclosingElement();
                String packageName = processingEnv.getElementUtils().getPackageOf(enclosingElement).toString();
                try {
                    //文件名：MainActivity_ViewBinding
                    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding");
                    writer = sourceFile.openWriter();
                    //        package com.example.annotation;
                    writer.write("package " + packageName + ";\n");
                    //        import com.example.annotation.IBinder;
                    writer.write("import " + packageName + ".IBinder;\n");
                    //        public class MainActivity_ViewBinding implements IBinder<com.example.annotation.MainActivity> {
                    writer.write("public class " + activityName + "_ViewBinding implements IBinder<" +
                            packageName + "." + activityName + ">{\n");
                    //            @Overrid
                    //            public void bind(com.example.annotation.MainActivity target) {
                    writer.write(" @Override\n" +
                            " public void bind(" + packageName + "." + activityName + " target){");

                    // target.mTvText = (android.widget.TextView) target.findViewById(2131231186);
                    for (VariableElement variableElement : variableElements) { //这里可能有多个注解的View控件，因此需要循环遍历
                        //得到名字
                        String variableName = variableElement.getSimpleName().toString();
                        //得到ID
                        int id = variableElement.getAnnotation(BindView.class).value();
                        //得到类型
                        TypeMirror typeMirror = variableElement.asType();
                        writer.write("target." + variableName + "=(" + typeMirror + ")target.findViewById(" + id + ");\n");
                    }

                    writer.write("\n}}");

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return false;
    }
}