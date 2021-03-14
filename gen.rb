# methods generator
# RoutingDSL
# (1..22).each do |index|
#   t = (1..index).map { |x| %Q{T#{x}: c.WeakTypeTag,} }.join(" \n")
#   ts = (1..index).map { |x| %Q{T#{x}} }.join(", ")
#   r = %Q{
#     def run#{index}[#{t} R: c.WeakTypeTag](c: whitebox.Context)
#                                               (f: c.Expr[(#{ts}) => R]): c.Expr[ExecutableRule] = {
#       generate(c)(f.tree)
#     }
#   }
#   puts r 
# end  

# (1..22).each do |index|
#   t = (1..index).map { |x| "T#{x}" }.join(", ")
#   r = %Q{implicit def ~>[#{t}, R](f: (#{t}) => R): ExecutableRule =
#       macro RoutingImpl.run#{index}[#{t}, R]}
#   puts r
# end
# Routable:
# (1..22).each do |index|
#   t = (1..index).map{ |x| "T#{x}" }.join(", ")
#   r = %Q{
#     def route[#{t}, R](body: (#{t}) => R): (#{t}) => AsyncResult = macro MoarImpl.routeImpl#{index}[#{t}, R]
#   }.lstrip
#   puts r
# end
# (1..22).each do |index|
#   t = (1..index).map { |x| %Q{T#{x}: c.WeakTypeTag} }.join(",\n")
#   ts = (1..index).map { |x| %Q{T#{x}} }.join(", ")
#   r = %Q{
#       def routeImpl#{index}[#{t}, R: c.WeakTypeTag](c: whitebox.Context)
#                                                         (body: c.Expr[(#{ts}) => R]): c.Expr[(#{ts}) => AsyncResult] = {
#         c.Expr[(#{ts}) => AsyncResult](generate(c)(body.tree))
#       }
#    }.lstrip
#    puts r
# end