
# One More Http Server

todo:
1. async - done
2. req as parameter to function  (check with unit functions) - done 
3. improve macro compilation ?
3.1 query materializer   
4. add more ~> parameters macro - done (check when AnyParam ~> () => unit fn)
5. streaming
6. http2
7. cookie param
8. swagger
9. resolve todos
10. websockets 


[] routing dsl
[] streaming 
[] swagger
[] websocket ? 


```scala

get("test" / LongParam) ~> { x: Long =>
  
}


```

### License: MIT

