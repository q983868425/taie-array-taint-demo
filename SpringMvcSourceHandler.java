
public class SpringMvcSourceHandler extends OnFlyHandler {

    SpringMvcSourceHandler(HandlerContext context) {
        super(context);
    }
  
    private Solver solver;

    @Override
    public void setSolver(Solver solver) {
        this.solver = solver;
    }

    @Override
    public void onStart() {
        List<JClass> list = this.solver.getHierarchy().applicationClasses().toList();
        for (JClass jClass : list) {
            List<JMethod> declaredMethods = jClass.getDeclaredMethods().stream().toList();
            for (JMethod jMethod : declaredMethods) {
                boolean isHandler = jMethod.getAnnotations().stream().anyMatch(annotation -> annotation.getType().matches("org.springframework.web.bind.annotation.\\w+Mapping"));
                if (!isHandler) {
                    continue;
                }
                solver.addEntryPoint(new EntryPoint(jMethod, EmptyParamProvider.get()));
            }
        }
    }
  
    @Override
    public void onNewCSMethod(CSMethod csMethod) {
        JMethod method = csMethod.getMethod();
        Context context = csMethod.getContext();
        boolean isMappingMethod = !method.getAnnotations()
                .stream()
                .filter(annotation ->
                        annotation.getType().matches("org.springframework.web.bind.annotation.\\w+Mapping"))
                .toList()
                .isEmpty();
        if (!isMappingMethod) {
            return;
        }
        IR ir = method.getIR();
        // 初始化this指针
        Var thisVar = ir.getThis();
        solver.addPointsTo(
                solver.getCSManager().getCSVar(csMethod.getContext(), thisVar),
                solver.getHeapModel().getMockObj(() ->
                        "CONTROLLER_INJECTION",
                        method.getDeclaringClass().getName(),
                        method.getDeclaringClass().getType()));
        // 将参数添加至Source点
        for (int i = 0; i < ir.getParams().size(); i++) {
            Var param = ir.getParam(i);
            IndexRef indexRef = new IndexRef(IndexRef.Kind.VAR, i, null);
            SourcePoint sourcePoint = new ParamSourcePoint(method, indexRef, new ParamSource(method, indexRef, method.getParamType(i), "{ kind: param, method: \"" + method.getSignature() + "\", index: " + i + "}"));
            Obj taint = manager.makeTaint(sourcePoint, param.getType());
            solver.addVarPointsTo(context, param, taint);
        }
    }
}
