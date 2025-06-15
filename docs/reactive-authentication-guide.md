# Reactive Authentication in Spring WebFlux vs Servlet

## 🔄 **Key Differences Overview**

| **Aspect** | **Spring MVC (Servlet)** | **Spring WebFlux (Reactive)** |
|------------|---------------------------|--------------------------------|
| **Threading Model** | One thread per request | Multiple threads per request |
| **Context Storage** | ThreadLocal | Reactive Context |
| **Authentication Access** | `SecurityContextHolder` | `ReactiveSecurityContextHolder` |
| **Return Type** | Direct objects | `Mono<T>` / `Flux<T>` |
| **Blocking** | Can block threads | Non-blocking |

## 🧵 **Why ThreadLocal Doesn't Work in Reactive**

### **Servlet Model (Traditional)**
```
Request → [Thread 1] → Controller → Service → Repository → Response
          ↑ ThreadLocal stores auth info here
```

### **Reactive Model (WebFlux)**
```
Request → [Thread 1] → Controller → [Thread 2] → Service → [Thread 3] → Repository → Response
          ↑ ThreadLocal lost!    ↑ Different thread!    ↑ Another thread!
```

**Problem**: In reactive programming, different parts of the request pipeline can run on different threads, so `ThreadLocal` (used by `SecurityContextHolder`) loses the authentication context.

## 🔐 **Authentication Patterns Comparison**

### **1. Basic Authentication Access**

#### **Servlet Way (Spring MVC)**
```java
@GetMapping("/user")
public String getUser() {
    // ✅ Works - same thread throughout request
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return "Hello " + auth.getName();
}
```

#### **Reactive Way (WebFlux)**
```java
@GetMapping("/user")
public Mono<String> getUser() {
    // ✅ Reactive context flows through stream
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getName)
        .map(name -> "Hello " + name);
}
```

### **2. Custom JWT Authentication**

#### **Servlet Approach**
```java
@PostMapping("/tasks")
public ResponseEntity<String> createTask(@RequestHeader("Authorization") String auth) {
    // ❌ Blocking operation
    String userId = jwtService.extractUserId(auth); // Synchronous
    
    // ❌ Blocking database call
    Task task = taskService.createTask(userId); // Synchronous
    
    return ResponseEntity.ok("Task created");
}
```

#### **Reactive Approach**
```java
@PostMapping("/tasks")
public Mono<String> createTask(@RequestHeader("Authorization") String auth) {
    // ✅ Non-blocking reactive chain
    return jwtService.extractUserIdReactive(auth)
        .flatMap(userId -> taskService.createTaskReactive(userId))
        .map(task -> "Task created: " + task.getId());
}
```

## 🚀 **Reactive Authentication Patterns**

### **Pattern 1: Simple Token Extraction**
```java
public Mono<String> extractUserId(String authHeader) {
    return Mono.fromCallable(() -> {
        // JWT parsing logic here
        return parseJwtAndExtractUserId(authHeader);
    })
    .subscribeOn(Schedulers.boundedElastic()); // For blocking operations
}
```

### **Pattern 2: Chained Authentication Operations**
```java
public Mono<UserInfo> authenticateAndLoadUser(String authHeader) {
    return validateToken(authHeader)
        .flatMap(this::extractClaims)
        .flatMap(claims -> loadUserFromDatabase(claims.getUserId()))
        .flatMap(user -> checkUserPermissions(user))
        .map(this::buildUserInfo);
}
```

### **Pattern 3: Error Handling**
```java
public Mono<String> secureEndpoint(String authHeader) {
    return authenticateUser(authHeader)
        .flatMap(user -> processBusinessLogic(user))
        .onErrorResume(AuthenticationException.class, 
            ex -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
        .onErrorResume(AuthorizationException.class,
            ex -> Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN)));
}
```

### **Pattern 4: Optional Authentication**
```java
public Mono<String> optionalAuth(String authHeader) {
    if (authHeader == null) {
        return Mono.just("Anonymous user");
    }
    
    return authenticateUser(authHeader)
        .map(user -> "Authenticated: " + user.getName())
        .switchIfEmpty(Mono.just("Anonymous user"))
        .onErrorReturn("Authentication failed - anonymous access");
}
```

## 🔧 **Practical Implementation Tips**

### **1. Don't Block in Reactive Streams**
```java
// ❌ BAD - Blocking in reactive context
public Mono<String> badExample(String authHeader) {
    String userId = blockingJwtService.extractUserId(authHeader); // Blocks!
    return Mono.just("Hello " + userId);
}

// ✅ GOOD - Fully reactive
public Mono<String> goodExample(String authHeader) {
    return reactiveJwtService.extractUserId(authHeader)
        .map(userId -> "Hello " + userId);
}
```

### **2. Use Proper Schedulers for Blocking Operations**
```java
public Mono<String> handleBlockingAuth(String authHeader) {
    return Mono.fromCallable(() -> {
        // If you must use blocking JWT library
        return blockingJwtLibrary.parse(authHeader);
    })
    .subscribeOn(Schedulers.boundedElastic()) // Use bounded elastic for blocking I/O
    .map(claims -> claims.getUserId());
}
```

### **3. Compose Authentication with Business Logic**
```java
public Mono<TaskResponse> createTaskWithAuth(String authHeader, TaskRequest request) {
    return authenticateUser(authHeader)
        .flatMap(user -> validateUserPermissions(user, "CREATE_TASK"))
        .flatMap(user -> taskService.createTask(request, user.getId()))
        .map(this::buildTaskResponse);
}
```

## 🔒 **When to Use Spring Security Reactive**

If you need full authentication/authorization features, consider adding Spring Security:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
```

Then you can use:
```java
@GetMapping("/secure")
public Mono<String> secureEndpoint() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .cast(JwtAuthenticationToken.class)
        .map(jwt -> jwt.getToken().getClaimAsString("sub"))
        .map(userId -> "Hello " + userId);
}
```

## 📝 **Best Practices Summary**

1. **Never use `SecurityContextHolder` in WebFlux** - it won't work correctly
2. **Use `ReactiveSecurityContextHolder`** when using Spring Security
3. **Make authentication operations reactive** - return `Mono<T>` or `Flux<T>`
4. **Chain operations with `flatMap`** instead of blocking calls
5. **Handle errors reactively** with `onErrorResume`, `onErrorReturn`
6. **Use proper schedulers** for any blocking operations
7. **Compose authentication with business logic** in reactive chains

## 🎯 **Your Current Setup**

In your current implementation:
- ✅ You have a reactive `JwtService` with both sync and async methods
- ✅ Your controller uses the reactive patterns correctly
- ✅ You're following non-blocking principles
- 🔄 Consider migrating fully to reactive authentication methods for better performance