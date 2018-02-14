# izettle-filters

## AdminDirectAccess

Prevents non-direct access to admin resources in Dropwizard.
That is, requests going via a load balancer.

### Usage

```java
bootstrap.addBundle(new AdminDirectAccessBundle());
```
