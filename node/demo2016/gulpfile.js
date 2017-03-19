var gulp = require('gulp');
var concat = require('gulp-concat');


gulp.task('default',  ['copy'])
gulp.task('copy',  ['default'])

gulp.task('watch', function(){
	gulp.watch('scripts/**/*.*', ['default']);
});

var paths = {
    bower: "./bower_components/",
    lib: "./public/"
};

gulp.task("copy", [], function () {
    var bower = {
        "swig":   "swig/lib/**/*.{js,map}"
    }
 
    for (var destinationDir in bower) {
        console.log( bower[destinationDir])
        gulp.src(paths.bower + bower[destinationDir])
          .pipe(gulp.dest(paths.lib + destinationDir  ));
    }
});
console.log("built");
